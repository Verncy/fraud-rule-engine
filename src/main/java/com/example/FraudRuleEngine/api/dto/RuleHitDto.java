package com.example.FraudRuleEngine.api.dto;

public record RuleHitDto(
        String ruleId,
        String ruleVersion,
        String severity,
        String reason
) {}
