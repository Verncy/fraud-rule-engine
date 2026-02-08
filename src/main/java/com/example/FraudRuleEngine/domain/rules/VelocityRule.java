package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;
import com.example.FraudRuleEngine.persistence.repo.TransactionRepository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

public class VelocityRule implements FraudRule {

    private final TransactionRepository transactionRepository;
    private final int maxCount;
    private final int windowMinutes;

    //  Correct constructor order: (repo, maxCount, windowMinutes)
    public VelocityRule(TransactionRepository transactionRepository, int maxCount, int windowMinutes) {
        this.transactionRepository = transactionRepository;
        this.maxCount = maxCount;
        this.windowMinutes = windowMinutes;
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
        OffsetDateTime end = event.eventTime();
        OffsetDateTime start = end.minusMinutes(windowMinutes);

        long count = transactionRepository.countByCustomerIdAndEventTimeBetween(
                event.customerId(),
                start,
                end
        );

        // Trigger ONLY when count EXCEEDS maxCount
        if (count > maxCount) {
            return Optional.of(new RuleHit(
                    id(),
                    version(),
                    severity(),
                    String.format(
                    "Velocity threshold exceeded: %d transactions in last %d minutes (max %d)",
                            count, windowMinutes, maxCount ),
                    Map.of(
                            "customerId", event.customerId(),
                            "windowMinutes", windowMinutes,
                            "start", start.toString(),
                            "end", end.toString(),
                            "count", count,
                            "maxCount", maxCount
                    )
            ));
        }

        return Optional.empty();
    }
}
