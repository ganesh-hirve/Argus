package com.argus.dto;

import com.argus.enums.CheckStatus;

public class ValidationContext {
    private CheckStatus taskExists = CheckStatus.NOT_EVALUATED;
    private CheckStatus agentAuthorization = CheckStatus.NOT_EVALUATED;
    private CheckStatus resourceAuthorization = CheckStatus.NOT_EVALUATED;
    private CheckStatus stateTransition = CheckStatus.NOT_EVALUATED;
    private CheckStatus budgetAvailability = CheckStatus.NOT_EVALUATED;
    private CheckStatus expiry = CheckStatus.NOT_EVALUATED;
    private CheckStatus duplicateExecution = CheckStatus.NOT_EVALUATED;
    private CheckStatus amountIntegrity = CheckStatus.NOT_EVALUATED;

    public CheckStatus getTaskExists() {
        return taskExists;
    }

    public void setTaskExists(CheckStatus taskExists) {
        this.taskExists = taskExists;
    }

    public CheckStatus getAgentAuthorization() {
        return agentAuthorization;
    }

    public void setAgentAuthorization(CheckStatus agentAuthorization) {
        this.agentAuthorization = agentAuthorization;
    }

    public CheckStatus getResourceAuthorization() {
        return resourceAuthorization;
    }

    public void setResourceAuthorization(CheckStatus resourceAuthorization) {
        this.resourceAuthorization = resourceAuthorization;
    }

    public CheckStatus getStateTransition() {
        return stateTransition;
    }

    public void setStateTransition(CheckStatus stateTransition) {
        this.stateTransition = stateTransition;
    }

    public CheckStatus getBudgetAvailability() {
        return budgetAvailability;
    }

    public void setBudgetAvailability(CheckStatus budgetAvailability) {
        this.budgetAvailability = budgetAvailability;
    }

    public CheckStatus getExpiry() {
        return expiry;
    }

    public void setExpiry(CheckStatus expiry) {
        this.expiry = expiry;
    }

    public CheckStatus getDuplicateExecution() {
        return duplicateExecution;
    }

    public void setDuplicateExecution(CheckStatus duplicateExecution) {
        this.duplicateExecution = duplicateExecution;
    }

    public CheckStatus getAmountIntegrity() {
        return amountIntegrity;
    }

    public void setAmountIntegrity(CheckStatus amountIntegrity) {
        this.amountIntegrity = amountIntegrity;
    }
}
