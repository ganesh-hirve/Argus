package com.argus.repository;

import com.argus.entity.ExecutionRecord;
import com.argus.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionRecordRepository
        extends JpaRepository<ExecutionRecord, Long> {

    boolean existsByTaskAuthorityIdAndResourceIdAndActionAndIdempotencyKey(
            Long taskAuthorityId,
            String resourceId,
            ActionType action,
            String idempotencyKey
    );
}