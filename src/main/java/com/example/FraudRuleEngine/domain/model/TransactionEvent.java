package com.example.FraudRuleEngine.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionEvent(
        String transactionId,
        String customerId,
        BigDecimal amount,
        String currency,
        String merchant,
        String category,
        OffsetDateTime eventTime
) {}
