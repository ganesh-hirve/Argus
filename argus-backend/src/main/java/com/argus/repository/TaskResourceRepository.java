package com.argus.repository;

import com.argus.entity.TaskResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskResourceRepository extends JpaRepository<TaskResource, Long> {

    Optional<TaskResource> findByTaskAuthority_TaskIdAndResourceId(
            String taskId,
            String resourceId
    );
}