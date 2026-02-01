package com.example.FraudRuleEngine.domain.rules;

import java.util.Map;

public record RuleHit(
        String ruleId,
        String ruleVersion,
        Severity severity,
        String reason,
        Map<String, Object> metadata
) {}
