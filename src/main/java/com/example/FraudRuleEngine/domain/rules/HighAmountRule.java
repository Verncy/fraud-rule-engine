package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class HighAmountRule implements FraudRule {

    private final BigDecimal threshold;

    public HighAmountRule(BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public String id() {
        return "HIGH_AMOUNT";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public Severity severity() {
        return Severity.HIGH;
    }

    @Override
    public Optional<RuleHit> evaluate(TransactionEvent event) {
        if (event.amount() != null && event.amount().compareTo(threshold) > 0) {
            return Optional.of(new RuleHit(
                    id(),
                    version(),
                    severity(),
                    "Amount exceeds configured threshold",
                    Map.of(
                            "threshold", threshold.toPlainString(),
                            "amount", event.amount().toPlainString()
                    )
            ));
        }
        return Optional.empty();
    }
}
