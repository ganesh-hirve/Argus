package com.argus.entity;

import com.argus.enums.ActionType;
import com.argus.enums.Decision;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "execution_record",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_execution_idempotency",
                        columnNames = {
                                "task_authority_id",
                                "resource_id",
                                "action",
                                "idempotency_key"
                        }
                )
        }
)
public class ExecutionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_authority_id", nullable = false)
    private TaskAuthority taskAuthority;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    // Amount involved in this execution (stored in paise)
    private Long amount;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // Required by JPA
    public ExecutionRecord() {
    }


    // ---------------- GETTERS ----------------

    public Long getId() {
        return id;
    }

    public TaskAuthority getTaskAuthority() {
        return taskAuthority;
    }

    public String getResourceId() {
        return resourceId;
    }

    public ActionType getAction() {
        return action;
    }

    public Long getAmount() {
        return amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Decision getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    // ---------------- SETTERS ----------------

    public void setTaskAuthority(TaskAuthority taskAuthority) {
        this.taskAuthority = taskAuthority;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}