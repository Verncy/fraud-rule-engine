package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;
import com.example.FraudRuleEngine.persistence.repo.TransactionRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public class VelocityRule implements FraudRule {

    private final TransactionRepository transactionRepository;
    private final int windowMinutes;
    private final int maxCount;

    public VelocityRule(TransactionRepository transactionRepository, int windowMinutes, int maxCount) {
        this.transactionRepository = transactionRepository;
        this.windowMinutes = windowMinutes;
        this.maxCount = maxCount;
    }

    @Override
    public Severity severity() {
        return Severity.HIGH;
    }

    @Override 
    public String id() { 
        return "VELOCITY"; 
    }

    @Override 
    public String version() { 
        return "1.0"; 
    }

    @Override
    public Optional<RuleHit> evaluate(TransactionEvent event) {
        

        if (event.eventTime() == null || event.customerId() == null) {
            return Optional.empty();
        }

        OffsetDateTime since = event.eventTime().minusMinutes(windowMinutes);

        long count = transactionRepository.countByCustomerIdAndEventTimeGreaterThanEqual(
                event.customerId(),
                since
        );

        if (count <= maxCount) return Optional.empty();

        return Optional.of(new RuleHit(
                id(),
                version(),
                severity(),
                "Too many transactions in " + windowMinutes + " minutes for customer " + event.customerId(),
                java.util.Map.of(
                        "customerId", event.customerId(),
                        "windowMinutes", windowMinutes,
                        "count", count,
                        "maxCount", maxCount
                )
        ));
    }
}
