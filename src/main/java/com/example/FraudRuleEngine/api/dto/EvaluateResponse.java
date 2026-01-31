package com.example.FraudRuleEngine.api.dto;

import java.util.List;

public record EvaluateResponse(
        String transactionId,
        boolean flagged,
        int riskScore,
        List<RuleHitDto> ruleHits
) {}
