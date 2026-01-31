package com.example.FraudRuleEngine.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionEventRequest(
        @NotBlank @Size(max = 64) String transactionId,
        @NotBlank @Size(max = 64) String customerId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(max = 8) String currency,
        @Size(max = 128) String merchant,
        @Size(max = 64) String category,
        @NotNull OffsetDateTime eventTime
) {}
