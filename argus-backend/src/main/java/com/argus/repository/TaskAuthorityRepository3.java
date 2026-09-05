package com.argus.repository;

import com.argus.entity.ExecutionRecord;
import com.argus.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskAuthorityRepository3
        extends JpaRepository<ExecutionRecord, Long> {

    Optional<ExecutionRecord>
    findByTaskAuthority_TaskIdAndResourceIdAndActionAndIdempotencyKey(
            String taskId,
            String resourceId,
            ActionType action,
            String idempotencyKey
    );
}