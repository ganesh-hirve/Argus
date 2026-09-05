package com.argus.dto;

import com.argus.enums.ActionType;

public class ExecutionRequest {

    private String taskId;

    private String agentId;

    private String resourceId;

    private ActionType action;

    // Amount in paise
    private Long amount;

    private String idempotencyKey;

    public ExecutionRequest() {
    }

    public ExecutionRequest(
            String taskId,
            String agentId,
            String resourceId,
            ActionType action,
            Long amount,
            String idempotencyKey
    ) {
        this.taskId = taskId;
        this.agentId = agentId;
        this.resourceId = resourceId;
        this.action = action;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}