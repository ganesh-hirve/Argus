package com.argus.service;

import com.argus.dto.ExecutionRequest;
import com.argus.dto.ExecutionResponse;
import com.argus.dto.RiskAssessment;
import com.argus.dto.ValidationContext;
import com.argus.enums.CheckStatus;
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
        private final RiskAssessmentService riskAssessmentService;

        public EnforcementService(
                        TaskAuthorityRepository taskAuthorityRepository,
                        TaskResourceRepository taskResourceRepository,
                        ExecutionRecordRepository executionRecordRepository,
                        RiskAssessmentService riskAssessmentService) {
                this.taskAuthorityRepository = taskAuthorityRepository;
                this.taskResourceRepository = taskResourceRepository;
                this.executionRecordRepository = executionRecordRepository;
                this.riskAssessmentService = riskAssessmentService;
        }

        // =========================================================
        // DRY RUN
        // =========================================================

        public ExecutionResponse evaluate(ExecutionRequest request) {
                return validateExecution(request);
        }

        // =========================================================
        // ACTUAL EXECUTION
        // =========================================================

        @Transactional
        public ExecutionResponse execute(ExecutionRequest request) {

                ValidationContext ctx = new ValidationContext();
                ctx.setTaskExists(CheckStatus.PASS);
                ctx.setAgentAuthorization(CheckStatus.PASS);
                ctx.setResourceAuthorization(CheckStatus.PASS);
                ctx.setStateTransition(CheckStatus.PASS);
                ctx.setBudgetAvailability(CheckStatus.PASS);
                ctx.setExpiry(CheckStatus.PASS);
                ctx.setDuplicateExecution(CheckStatus.PASS);
                ctx.setAmountIntegrity(CheckStatus.PASS);


                ExecutionResponse validationResult = validateExecution(request);

                if (validationResult.getDecision() == Decision.BLOCKED) {
                        return validationResult;
                }

                Optional<TaskAuthority> taskOptional = taskAuthorityRepository.findByTaskId(
                                request.getTaskId());

                ctx.setTaskExists(CheckStatus.FAIL);
                if (taskOptional.isEmpty()) {
                        return blocked("Task authority not found", request, null, ctx);
                }

                ctx.setTaskExists(CheckStatus.PASS);

                TaskAuthority task = taskOptional.get();

                ctx.setExpiry(CheckStatus.PASS);

                // -----------------------------------------------------
                // 6. RESOURCE AUTHORIZATION
                // -----------------------------------------------------

                Optional<TaskResource> resourceOptional = taskResourceRepository
                                .findByTaskAuthority_TaskIdAndResourceId(
                                                task.getTaskId(),
                                                request.getResourceId());

                ctx.setResourceAuthorization(CheckStatus.FAIL);
                if (resourceOptional.isEmpty()) {
                        return blocked("Resource not found", request, task, ctx);
                }

                ctx.setResourceAuthorization(CheckStatus.PASS);

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
                                        "Resource successfully verified");

                        return allowed("Verification completed successfully", request, task, ctx);
                }

                // =====================================================
                // EXECUTE PAYMENT
                // VERIFIED -> PAID
                // =====================================================

                ctx.setStateTransition(CheckStatus.PASS);

                // -----------------------------------------------------
                // 9. PAYMENT VALIDATION
                // -----------------------------------------------------

                if (request.getAction() == ActionType.PAY) {

                        Long amount = request.getAmount();

                        task.setConsumedBudget(
                                        task.getConsumedBudget() + amount);

                        resource.setState(ResourceState.PAID);

                        taskAuthorityRepository.save(task);
                        taskResourceRepository.save(resource);

                        createExecutionRecord(
                                        task,
                                        resource,
                                        request,
                                        Decision.ALLOWED,
                                        "Payment successfully executed");

                        return allowed("Payment executed successfully", request, task, ctx);
                }

                return blocked("Unsupported action", request, task, ctx);
        }

        // =========================================================
        // CORE VALIDATION ENGINE
        // =========================================================

        private ExecutionResponse validateExecution(
                        ExecutionRequest request) {

                ValidationContext ctx = new ValidationContext();

                // -----------------------------------------------------
                // 1. REQUEST VALIDATION
                // -----------------------------------------------------

                if (request == null) {
                        return blocked("Invalid request", null, null, ctx);
                }

                if (isBlank(request.getTaskId())) {
                        return blocked("Task ID is required", request, null, ctx);
                }

                if (isBlank(request.getAgentId())) {
                        return blocked("Agent ID is required", request, null, ctx);
                }

                if (isBlank(request.getResourceId())) {
                        return blocked("Resource ID is required", request, null, ctx);
                }

                if (request.getAction() == null) {
                        return blocked("Action is required", request, null, ctx);
                }

                if (isBlank(request.getIdempotencyKey())) {
                        return blocked("Idempotency key is required", request, null, ctx);
                }

                // -----------------------------------------------------
                // 2. FIND TASK
                // -----------------------------------------------------

                Optional<TaskAuthority> taskOptional = taskAuthorityRepository.findByTaskId(
                                request.getTaskId());

                ctx.setTaskExists(CheckStatus.FAIL);
                if (taskOptional.isEmpty()) {
                        return blocked("Task authority not found", request, null, ctx);
                }

                ctx.setTaskExists(CheckStatus.PASS);

                TaskAuthority task = taskOptional.get();

                // -----------------------------------------------------
                // 3. AGENT AUTHORIZATION
                // -----------------------------------------------------

                boolean authorizedAgent = task.getAgentId().equals(
                                request.getAgentId());

                ctx.setAgentAuthorization(CheckStatus.FAIL);
                if (!authorizedAgent) {
                        return blocked("Agent is not authorized for this task", request, task, ctx);
                }

                // -----------------------------------------------------
                // 4. TASK STATUS
                // -----------------------------------------------------

                ctx.setAgentAuthorization(CheckStatus.PASS);

                // -----------------------------------------------------
                // 4. TASK STATUS
                // -----------------------------------------------------

                if (task.getStatus() != TaskStatus.ACTIVE) {
                        return blocked("Task is not active. Current status: "
                                                        + task.getStatus(), request, task, ctx);
                }

                // -----------------------------------------------------
                // 5. EXPIRY
                // -----------------------------------------------------

                boolean expired = task.getExpiresAt() != null
                                && LocalDateTime.now().isAfter(
                                                task.getExpiresAt());

                ctx.setExpiry(CheckStatus.FAIL);
                if (expired) {
                        return blocked("Task authority has expired", request, task, ctx);
                }

                // -----------------------------------------------------
                // 6. RESOURCE AUTHORIZATION
                // -----------------------------------------------------

                ctx.setExpiry(CheckStatus.PASS);

                // -----------------------------------------------------
                // 6. RESOURCE AUTHORIZATION
                // -----------------------------------------------------

                Optional<TaskResource> resourceOptional = taskResourceRepository
                                .findByTaskAuthority_TaskIdAndResourceId(
                                                task.getTaskId(),
                                                request.getResourceId());

                ctx.setResourceAuthorization(CheckStatus.FAIL);
                if (resourceOptional.isEmpty()) {
                        return blocked("Resource is not authorized for this task", request, task, ctx);
                }

                ctx.setResourceAuthorization(CheckStatus.PASS);

                TaskResource resource = resourceOptional.get();

                // -----------------------------------------------------
                // 7. IDEMPOTENCY / REPLAY PROTECTION
                // -----------------------------------------------------

                boolean duplicate = executionRecordRepository
                                .existsByTaskAuthorityIdAndResourceIdAndActionAndIdempotencyKey(
                                                task.getId(),
                                                resource.getResourceId(),
                                                request.getAction(),
                                                request.getIdempotencyKey());

                ctx.setDuplicateExecution(CheckStatus.FAIL);
                if (duplicate) {
                        return blocked("Duplicate execution request detected", request, task, ctx);
                }

                // -----------------------------------------------------
                // 8. STATE TRANSITION
                // -----------------------------------------------------

                ctx.setDuplicateExecution(CheckStatus.PASS);

                // -----------------------------------------------------
                // 8. STATE TRANSITION
                // -----------------------------------------------------

                boolean validStateTransition = isActionAllowedForState(
                                request.getAction(),
                                resource.getState());

                ctx.setStateTransition(CheckStatus.FAIL);
                if (!validStateTransition) {
                        return blocked("Action " + request.getAction()
                                                        + " is not allowed when resource state is "
                                                        + resource.getState(), request, task, ctx);
                }

                // -----------------------------------------------------
                // 9. PAYMENT VALIDATION
                // -----------------------------------------------------

                ctx.setStateTransition(CheckStatus.PASS);

                // -----------------------------------------------------
                // 9. PAYMENT VALIDATION
                // -----------------------------------------------------

                if (request.getAction() == ActionType.PAY) {

                        // Invalid amount
                        if (request.getAmount() == null
                                        || request.getAmount() <= 0) {

                                return blocked("Valid payment amount is required", request, task, ctx);
                        }

                        // Amount manipulation
                        ctx.setAmountIntegrity(CheckStatus.FAIL);
                        if (!request.getAmount()
                                        .equals(resource.getAmount())) {

                                return blocked("Requested amount does not match authorized resource amount", request, task, ctx);
                        }

                        // Budget enforcement
                        ctx.setAmountIntegrity(CheckStatus.PASS);
                        ctx.setBudgetAvailability(CheckStatus.FAIL);
                        if (request.getAmount() > task.getAvailableBudget()) {

                                return blocked("Insufficient available task budget", request, task, ctx);
                        }
                }

                // -----------------------------------------------------
                // ALL SECURITY CHECKS PASSED
                // -----------------------------------------------------

                ctx.setBudgetAvailability(CheckStatus.PASS);
                ctx.setAmountIntegrity(CheckStatus.PASS);
                return allowed("Execution authorized", request, task, ctx);
        }

        // =========================================================
        // CREATE IMMUTABLE EXECUTION RECORD
        // =========================================================

        private void createExecutionRecord(
                        TaskAuthority task,
                        TaskResource resource,
                        ExecutionRequest request,
                        Decision decision,
                        String reason) {

                ExecutionRecord record = new ExecutionRecord();

                record.setTaskAuthority(task);

                record.setResourceId(
                                resource.getResourceId());

                record.setAction(
                                request.getAction());

                record.setAmount(
                                request.getAmount());

                record.setIdempotencyKey(
                                request.getIdempotencyKey());

                record.setDecision(
                                decision);

                record.setReason(
                                reason);

                executionRecordRepository.save(record);
        }

        // =========================================================
        // RESOURCE STATE MACHINE
        // =========================================================

        private boolean isActionAllowedForState(
                        ActionType action,
                        ResourceState state) {

                if (action == ActionType.VERIFY) {
                        return state == ResourceState.PENDING;
                }

                if (action == ActionType.PAY) {
                        return state == ResourceState.VERIFIED;
                }

                return false;
        }

        // =========================================================
        // RESPONSE BUILDERS
        // =========================================================

        private ExecutionResponse allowed(
                        String reason,
                        ExecutionRequest request,
                        TaskAuthority task,
                        ValidationContext ctx) {

                RiskAssessment riskAssessment = riskAssessmentService.assess(ctx);

                return new ExecutionResponse(
                                Decision.ALLOWED,
                                reason,
                                request != null
                                                ? request.getTaskId()
                                                : null,
                                request != null
                                                ? request.getResourceId()
                                                : null,
                                task != null
                                                ? task.getAvailableBudget()
                                                : null,
                                riskAssessment);
        }

        private ExecutionResponse blocked(
                        String reason,
                        ExecutionRequest request,
                        TaskAuthority task,
                        ValidationContext ctx) {

                RiskAssessment riskAssessment = riskAssessmentService.assess(ctx);

                return new ExecutionResponse(
                                Decision.BLOCKED,
                                reason,
                                request != null
                                                ? request.getTaskId()
                                                : null,
                                request != null
                                                ? request.getResourceId()
                                                : null,
                                task != null
                                                ? task.getAvailableBudget()
                                                : null,
                                riskAssessment);
        }

        // =========================================================
        // UTILITY
        // =========================================================

        private boolean isBlank(String value) {
                return value == null
                                || value.trim().isEmpty();
        }
}