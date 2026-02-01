package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;

import java.util.Optional;

public interface FraudRule {

    String id();

    String version();

    Severity severity();

    Optional<RuleHit> evaluate(TransactionEvent event);
}
