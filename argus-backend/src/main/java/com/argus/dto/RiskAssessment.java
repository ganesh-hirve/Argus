package com.argus.dto;

import com.argus.enums.RiskLevel;
import com.argus.enums.RiskSignal;

import java.util.List;

public class RiskAssessment {

    private int riskScore;
    private RiskLevel riskLevel;
    private List<RiskSignal> signals;

    public RiskAssessment(
            int riskScore,
            RiskLevel riskLevel,
            List<RiskSignal> signals
    ) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.signals = signals;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public List<RiskSignal> getSignals() {
        return signals;
    }
}