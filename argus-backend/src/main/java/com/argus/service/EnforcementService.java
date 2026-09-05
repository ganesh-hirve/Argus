package com.argus.service;

import com.argus.dto.ExecutionRequest;
import com.argus.dto.ExecutionResponse;
import com.argus.entity.ExecutionRecord;
import com.argus.entity.TaskAuthority;
import com.argus.entity.TaskResource;
import com.argus.enums.ActionType;
import com.argus.enums.Decision;
import com.argus.enums.ResourceState;
import com.argus.enums.TaskStatus;
import com.argus.repository.ExecutionRecordRepository;
import com.argus.repository.TaskAuthorityRepository;
import com.argus.repository.TaskResourceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EnforcementService {

    private final TaskAuthorityRepository taskAuthorityRepository;
    private final TaskResourceRepository taskResourceRepository;
    private final ExecutionRecordRepository executionRecordRepository;


    public EnforcementService(
            TaskAuthorityRepository taskAuthorityRepository,
            TaskResourceRepository taskResourceRepository,
            ExecutionRecordRepository executionRecordRepository
    ) {
        this.taskAuthorityRepository = taskAuthorityRepository;
        this.taskResourceRepository = taskResourceRepository;
        this.executionRecordRepository = executionRecordRepository;
    }


    // =========================================================
    // DRY RUN
    // Checks authorization without modifying database
    // =========================================================

    public ExecutionResponse evaluate(ExecutionRequest request) {
        return validateExecution(request);
    }


    // =========================================================
    // ACTUAL EXECUTION
    // Modifies database state
    // =========================================================

    @Transactional
    public ExecutionResponse execute(ExecutionRequest request) {

        // Step 1: Validate entire execution request
        ExecutionResponse validationResult = validateExecution(request);

        if (validationResult.getDecision() == Decision.BLOCKED) {
            return validationResult;
        }


        // Step 2: Fetch task authority
        Optional<TaskAuthority> taskOptional =
                taskAuthorityRepository.findByTaskId(
                        request.getTaskId()
                );

        if (taskOptional.isEmpty()) {
            return blocked(
                    "Task authority not found",
                    request.getTaskId(),
                    request.getResourceId(),
                    null
            );
        }

        TaskAuthority task = taskOptional.get();


        // Step 3: Fetch authorized resource
        Optional<TaskResource> resourceOptional =
                taskResourceRepository
                        .findByTaskAuthority_TaskIdAndResourceId(
                                task.getTaskId(),
                                request.getResourceId()
                        );

        if (resourceOptional.isEmpty()) {
            return blocked(
                    "Resource not found",
                    task.getTaskId(),
                    request.getResourceId(),
                    task.getAvailableBudget()
            );
        }

        TaskResource resource = resourceOptional.get();


        // =====================================================
        // EXECUTE VERIFY
        // PENDING -> VERIFIED
        // =====================================================

        if (request.getAction() == ActionType.VERIFY) {

            resource.setState(ResourceState.VERIFIED);

            taskResourceRepository.save(resource);

            createExecutionRecord(
                    task,
                    resource,
                    request,
                    Decision.ALLOWED,
                    "Resource successfully verified"
            );

            return allowed(
                    "Verification completed successfully",
                    task.getTaskId(),
                    resource.getResourceId(),
                    task.getAvailableBudget()
            );
        }


        // =====================================================
        // EXECUTE PAY
        // VERIFIED -> PAID
        // Budget gets consumed
        // =====================================================

        if (request.getAction() == ActionType.PAY) {

            Long amount = request.getAmount();


            // Consume task authority budget
            task.setConsumedBudget(
                    task.getConsumedBudget() + amount
            );


            // Update resource lifecycle
            resource.setState(ResourceState.PAID);


            // Persist state changes
            taskAuthorityRepository.save(task);
            taskResourceRepository.save(resource);


            // Create immutable execution record
            createExecutionRecord(
                    task,
                    resource,
                    request,
                    Decision.ALLOWED,
                    "Payment successfully executed"
            );


            return allowed(
                    "Payment executed successfully",
                    task.getTaskId(),
                    resource.getResourceId(),
                    task.getAvailableBudget()
            );
        }


        return blocked(
                "Unsupported action",
                task.getTaskId(),
                resource.getResourceId(),
                task.getAvailableBudget()
        );
    }


    // =========================================================
    // CORE VALIDATION ENGINE
    // =========================================================

    private ExecutionResponse validateExecution(
            ExecutionRequest request
    ) {

        // -----------------------------------------------------
        // 1. Validate request
        // -----------------------------------------------------

        if (request == null) {
            return blocked(
                    "Invalid request",
                    null,
                    null,
                    null
            );
        }


        if (isBlank(request.getTaskId())) {
            return blocked(
                    "Task ID is required",
                    null,
                    null,
                    null
            );
        }


        if (isBlank(request.getAgentId())) {
            return blocked(
                    "Agent ID is required",
                    request.getTaskId(),
                    request.getResourceId(),
                    null
            );
        }


        if (isBlank(request.getResourceId())) {
            return blocked(
                    "Resource ID is required",
                    request.getTaskId(),
                    null,
                    null
            );
        }


        if (request.getAction() == null) {
            return blocked(
                    "Action is required",
                    request.getTaskId(),
                    request.getResourceId(),
                    null
            );
        }


        if (isBlank(request.getIdempotencyKey())) {
            return blocked(
                    "Idempotency key is required",
                    request.getTaskId(),
                    request.getResourceId(),
                    null
            );
        }


        // -----------------------------------------------------
        // 2. Find task authority
        // -----------------------------------------------------

        Optional<TaskAuthority> taskOptional =
                taskAuthorityRepository.findByTaskId(
                        request.getTaskId()
                );

        if (taskOptional.isEmpty()) {
            return blocked(
                    "Task authority not found",
                    request.getTaskId(),
                    request.getResourceId(),
                    null
            );
        }

        TaskAuthority task = taskOptional.get();


        // -----------------------------------------------------
        // 3. Verify agent authorization
        // -----------------------------------------------------

        if (!task.getAgentId().equals(request.getAgentId())) {

            return blocked(
                    "Agent is not authorized for this task",
                    task.getTaskId(),
                    request.getResourceId(),
                    task.getAvailableBudget()
            );
        }


        // -----------------------------------------------------
        // 4. Verify task status
        // -----------------------------------------------------

        if (task.getStatus() != TaskStatus.ACTIVE) {

            return blocked(
                    "Task is not active. Current status: "
                            + task.getStatus(),
                    task.getTaskId(),
                    request.getResourceId(),
                    task.getAvailableBudget()
            );
        }


        // -----------------------------------------------------
        // 5. Check authority expiry
        // -----------------------------------------------------

        if (task.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(
                        task.getExpiresAt()
                )) {

            return blocked(
                    "Task authority has expired",
                    task.getTaskId(),
                    request.getResourceId(),
                    task.getAvailableBudget()
            );
        }


        // -----------------------------------------------------
        // 6. Verify resource belongs to task authority
        // -----------------------------------------------------

        Optional<TaskResource> resourceOptional =
                taskResourceRepository
                        .findByTaskAuthority_TaskIdAndResourceId(
                                task.getTaskId(),
                                request.getResourceId()
                        );

        if (resourceOptional.isEmpty()) {

            return blocked(
                    "Resource is not authorized for this task",
                    task.getTaskId(),
                    request.getResourceId(),
                    task.getAvailableBudget()
            );
        }

        TaskResource resource = resourceOptional.get();


        // -----------------------------------------------------
        // 7. Idempotency / Replay protection
        // IMPORTANT:
        // Must happen BEFORE lifecycle validation.
        // Otherwise replayed payment returns invalid state
        // instead of duplicate execution.
        // -----------------------------------------------------

        boolean duplicate =
                executionRecordRepository
                        .existsByTaskAuthorityIdAndResourceIdAndActionAndIdempotencyKey(
                                task.getId(),
                                resource.getResourceId(),
                                request.getAction(),
                                request.getIdempotencyKey()
                        );

        if (duplicate) {

            return blocked(
                    "Duplicate execution request detected",
                    task.getTaskId(),
                    resource.getResourceId(),
                    task.getAvailableBudget()
            );
        }


        // -----------------------------------------------------
        // 8. Validate resource lifecycle transition
        // -----------------------------------------------------

        if (!isActionAllowedForState(
                request.getAction(),
                resource.getState()
        )) {

            return blocked(
                    "Action " + request.getAction()
                            + " is not allowed when resource state is "
                            + resource.getState(),
                    task.getTaskId(),
                    resource.getResourceId(),
                    task.getAvailableBudget()
            );
        }


        // -----------------------------------------------------
        // 9. Payment-specific validation
        // -----------------------------------------------------

        if (request.getAction() == ActionType.PAY) {

            // Valid amount required
            if (request.getAmount() == null ||
                    request.getAmount() <= 0) {

                return blocked(
                        "Valid payment amount is required",
                        task.getTaskId(),
                        resource.getResourceId(),
                        task.getAvailableBudget()
                );
            }


            // Exact authorized amount enforcement
            if (!request.getAmount()
                    .equals(resource.getAmount())) {

                return blocked(
                        "Requested amount does not match authorized resource amount",
                        task.getTaskId(),
                        resource.getResourceId(),
                        task.getAvailableBudget()
                );
            }


            // Stateful budget enforcement
            if (request.getAmount() >
                    task.getAvailableBudget()) {

                return blocked(
                        "Insufficient available task budget",
                        task.getTaskId(),
                        resource.getResourceId(),
                        task.getAvailableBudget()
                );
            }
        }


        // -----------------------------------------------------
        // ALL SECURITY CHECKS PASSED
        // -----------------------------------------------------

        return allowed(
                "Execution authorized",
                task.getTaskId(),
                resource.getResourceId(),
                task.getAvailableBudget()
        );
    }


    // =========================================================
    // CREATE IMMUTABLE EXECUTION RECORD
    // =========================================================

    private void createExecutionRecord(
            TaskAuthority task,
            TaskResource resource,
            ExecutionRequest request,
            Decision decision,
            String reason
    ) {

        ExecutionRecord record =
                new ExecutionRecord();

        record.setTaskAuthority(task);

        record.setResourceId(
                resource.getResourceId()
        );

        record.setAction(
                request.getAction()
        );

        record.setAmount(
                request.getAmount()
        );

        record.setIdempotencyKey(
                request.getIdempotencyKey()
        );

        record.setDecision(
                decision
        );

        record.setReason(
                reason
        );

        executionRecordRepository.save(record);
    }


    // =========================================================
    // RESOURCE LIFECYCLE STATE MACHINE
    // =========================================================

    private boolean isActionAllowedForState(
            ActionType action,
            ResourceState state
    ) {

        if (action == ActionType.VERIFY) {
            return state == ResourceState.PENDING;
        }

        if (action == ActionType.PAY) {
            return state == ResourceState.VERIFIED;
        }

        return false;
    }


    // =========================================================
    // UTILITY METHODS
    // =========================================================

    private boolean isBlank(String value) {
        return value == null ||
                value.trim().isEmpty();
    }


    private ExecutionResponse allowed(
            String reason,
            String taskId,
            String resourceId,
            Long availableBudget
    ) {

        return new ExecutionResponse(
                Decision.ALLOWED,
                reason,
                taskId,
                resourceId,
                availableBudget
        );
    }


    private ExecutionResponse blocked(
            String reason,
            String taskId,
            String resourceId,
            Long availableBudget
    ) {

        return new ExecutionResponse(
                Decision.BLOCKED,
                reason,
                taskId,
                resourceId,
                availableBudget
        );
    }
}