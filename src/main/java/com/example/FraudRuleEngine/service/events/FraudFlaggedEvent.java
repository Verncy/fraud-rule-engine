package com.example.FraudRuleEngine.service.events;

import java.time.OffsetDateTime;
import java.util.List;

public record FraudFlaggedEvent(
        String transactionId,
        int riskScore,
        boolean anyHigh,
        String customerId,
        String merchant,
        String currency,
        Number amount,
        OffsetDateTime eventTime,
        List<String> ruleIds
) {}
