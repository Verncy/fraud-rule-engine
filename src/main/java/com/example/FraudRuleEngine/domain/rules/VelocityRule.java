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
        if (event == null || event.customerId() == null || event.eventTime() == null) {
            return Optional.empty();
        }

        OffsetDateTime end = event.eventTime();
        OffsetDateTime start = end.minusMinutes(windowMinutes);

        long count = transactionRepository.countByCustomerIdAndEventTimeBetween(
                event.customerId(),
                start,
                end
        );

        // Because you save the transaction BEFORE evaluating rules, the count includes the current tx.
        // So this rule triggers when the number of tx in the window is greater than maxCount.
        if (count <= maxCount) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                id(),
                version(),
                severity(),
                "Too many transactions in " + windowMinutes + " minutes for customer " + event.customerId(),
                java.util.Map.of(
                        "customerId", event.customerId(),
                        "windowMinutes", windowMinutes,
                        "start", start.toString(),
                        "end", end.toString(),
                        "count", count,
                        "maxCount", maxCount
                )
        ));
    }
}
