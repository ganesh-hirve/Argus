package com.argus.dto;

import com.argus.enums.Decision;

public class ExecutionResponse {

    private Decision decision;

    private String reason;

    private String taskId;

    private String resourceId;

    private Long availableBudget;

    private RiskAssessment riskAssessment;


    public ExecutionResponse() {

    }


    public ExecutionResponse(

            Decision decision,

            String reason,

            String taskId,

            String resourceId,

            Long availableBudget,

            RiskAssessment riskAssessment

    ) {

        this.decision = decision;
        this.reason = reason;
        this.taskId = taskId;
        this.resourceId = resourceId;
        this.availableBudget = availableBudget;
        this.riskAssessment = riskAssessment;

    }


    public Decision getDecision() {

        return decision;

    }


    public void setDecision(Decision decision) {

        this.decision = decision;

    }


    public String getReason() {

        return reason;

    }


    public void setReason(String reason) {

        this.reason = reason;

    }


    public String getTaskId() {

        return taskId;

    }


    public void setTaskId(String taskId) {

        this.taskId = taskId;

    }


    public String getResourceId() {

        return resourceId;

    }


    public void setResourceId(String resourceId) {

        this.resourceId = resourceId;

    }


    public Long getAvailableBudget() {

        return availableBudget;

    }


    public void setAvailableBudget(Long availableBudget) {

        this.availableBudget = availableBudget;

    }


    public RiskAssessment getRiskAssessment() {

        return riskAssessment;

    }


    public void setRiskAssessment(
            RiskAssessment riskAssessment
    ) {

        this.riskAssessment = riskAssessment;

    }

}