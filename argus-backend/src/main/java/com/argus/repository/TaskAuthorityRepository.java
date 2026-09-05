package com.argus.repository;

import com.argus.entity.TaskAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskAuthorityRepository extends JpaRepository<TaskAuthority, Long> {

    Optional<TaskAuthority> findByTaskId(String taskId);
}