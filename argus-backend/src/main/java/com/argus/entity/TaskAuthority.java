package com.argus.entity;

import com.argus.enums.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task_authority")
public class TaskAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Prevents lost updates during concurrent execution
    @Version
    private Long version;

    @Column(unique = true, nullable = false, updatable = false)
    private String taskId;

    @Column(nullable = false)
    private String agentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    // All monetary values stored in paise
    @Column(nullable = false)
    private Long maxBudget;

    @Column(nullable = false)
    private Long consumedBudget;

    @Column(nullable = false)
    private Long reservedBudget;

    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "taskAuthority",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TaskResource> resources = new ArrayList<>();


    // Required by JPA
    public TaskAuthority() {
    }


    // ---------------- RELATIONSHIP HELPER ----------------

    public void addResource(TaskResource resource) {
        resources.add(resource);
        resource.setTaskAuthority(this);
    }

    public void removeResource(TaskResource resource) {
        resources.remove(resource);
        resource.setTaskAuthority(null);
    }


    // ---------------- BUSINESS LOGIC ----------------

    public Long getAvailableBudget() {

        long consumed = consumedBudget != null ? consumedBudget : 0L;
        long reserved = reservedBudget != null ? reservedBudget : 0L;

        return maxBudget - consumed - reserved;
    }


    // ---------------- GETTERS ----------------

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getAgentId() {
        return agentId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Long getMaxBudget() {
        return maxBudget;
    }

    public Long getConsumedBudget() {
        return consumedBudget;
    }

    public Long getReservedBudget() {
        return reservedBudget;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<TaskResource> getResources() {
        return resources;
    }


    // ---------------- SETTERS ----------------

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setMaxBudget(Long maxBudget) {
        this.maxBudget = maxBudget;
    }

    public void setConsumedBudget(Long consumedBudget) {
        this.consumedBudget = consumedBudget;
    }

    public void setReservedBudget(Long reservedBudget) {
        this.reservedBudget = reservedBudget;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setResources(List<TaskResource> resources) {
        this.resources = resources;
    }


    // ---------------- JPA LIFECYCLE ----------------

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (consumedBudget == null) {
            consumedBudget = 0L;
        }

        if (reservedBudget == null) {
            reservedBudget = 0L;
        }

        if (status == null) {
            status = TaskStatus.ACTIVE;
        }
    }
}