package com.argus.service;

import com.argus.dto.RiskAssessment;
import com.argus.dto.ValidationContext;
import com.argus.enums.CheckStatus;
import com.argus.enums.RiskLevel;
import com.argus.enums.RiskSignal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskAssessmentService {

    // =========================================================
    // VIOLATION RISK WEIGHTS
    // =========================================================

    private static final int UNKNOWN_TASK_RISK = 40;
    private static final int UNAUTHORIZED_AGENT_RISK = 35;
    private static final int UNAUTHORIZED_RESOURCE_RISK = 30;
    private static final int INVALID_STATE_RISK = 20;
    private static final int AMOUNT_MANIPULATION_RISK = 40;
    private static final int INSUFFICIENT_BUDGET_RISK = 30;
    private static final int EXPIRED_AUTHORITY_RISK = 35;
    private static final int DUPLICATE_EXECUTION_RISK = 25;


    public RiskAssessment assess(ValidationContext ctx) {

        int riskScore = 0;

        List<RiskSignal> signals = new ArrayList<>();


        // =====================================================
        // TASK EXISTENCE
        // =====================================================

        if (ctx.getTaskExists() == CheckStatus.FAIL) {
            riskScore += UNKNOWN_TASK_RISK;
            signals.add(RiskSignal.UNKNOWN_TASK);
        }


        // =====================================================
        // AGENT AUTHORIZATION
        // =====================================================

        if (ctx.getAgentAuthorization() == CheckStatus.PASS) {
            signals.add(RiskSignal.AUTHORIZED_AGENT);
        } else if (ctx.getAgentAuthorization() == CheckStatus.FAIL) {
            riskScore += UNAUTHORIZED_AGENT_RISK;
            signals.add(RiskSignal.UNAUTHORIZED_AGENT);
        }


        // =====================================================
        // RESOURCE AUTHORIZATION
        // =====================================================

        if (ctx.getResourceAuthorization() == CheckStatus.PASS) {
            signals.add(RiskSignal.AUTHORIZED_RESOURCE);
        } else if (ctx.getResourceAuthorization() == CheckStatus.FAIL) {
            riskScore += UNAUTHORIZED_RESOURCE_RISK;
            signals.add(RiskSignal.UNAUTHORIZED_RESOURCE);
        }


        // =====================================================
        // STATE TRANSITION
        // =====================================================

        if (ctx.getStateTransition() == CheckStatus.PASS) {
            signals.add(RiskSignal.VALID_STATE_TRANSITION);
        } else if (ctx.getStateTransition() == CheckStatus.FAIL) {
            riskScore += INVALID_STATE_RISK;
            signals.add(RiskSignal.INVALID_STATE_TRANSITION);
        }


        // =====================================================
        // BUDGET AUTHORITY
        // =====================================================

        if (ctx.getBudgetAvailability() == CheckStatus.PASS) {
            signals.add(RiskSignal.WITHIN_BUDGET);
        } else if (ctx.getBudgetAvailability() == CheckStatus.FAIL) {
            riskScore += INSUFFICIENT_BUDGET_RISK;
            signals.add(RiskSignal.INSUFFICIENT_BUDGET);
        }


        // =====================================================
        // FINANCIAL INTEGRITY
        // =====================================================

        if (ctx.getAmountIntegrity() == CheckStatus.FAIL) {
            riskScore += AMOUNT_MANIPULATION_RISK;
            signals.add(RiskSignal.AMOUNT_MANIPULATION);
        }


        // =====================================================
        // TEMPORAL AUTHORITY
        // =====================================================

        if (ctx.getExpiry() == CheckStatus.FAIL) {
            riskScore += EXPIRED_AUTHORITY_RISK;
            signals.add(RiskSignal.EXPIRED_AUTHORITY);
        }


        // =====================================================
        // REPLAY PROTECTION
        // =====================================================

        if (ctx.getDuplicateExecution() == CheckStatus.FAIL) {
            riskScore += DUPLICATE_EXECUTION_RISK;
            signals.add(RiskSignal.DUPLICATE_EXECUTION);
        }


        // =====================================================
        // NORMALIZE SCORE
        // =====================================================

        riskScore = Math.min(riskScore, 100);


        // =====================================================
        // DETERMINE RISK LEVEL
        // =====================================================

        RiskLevel riskLevel = determineRiskLevel(riskScore);


        return new RiskAssessment(
                riskScore,
                riskLevel,
                signals
        );
    }


    // =========================================================
    // RISK LEVEL CLASSIFICATION
    // =========================================================

    private RiskLevel determineRiskLevel(int riskScore) {

        if (riskScore <= 20) {
            return RiskLevel.LOW;
        }

        if (riskScore <= 50) {
            return RiskLevel.MEDIUM;
        }

        if (riskScore <= 75) {
            return RiskLevel.HIGH;
        }

        return RiskLevel.CRITICAL;
    }
}