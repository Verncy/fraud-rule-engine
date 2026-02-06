package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HighAmountRuleTest {

    @Test
    void whenAmountAboveThreshold_thenReturnsRuleHit() {
        HighAmountRule rule = new HighAmountRule(new BigDecimal("50000"));

        TransactionEvent event = new TransactionEvent(
                "tx-1",
                "cust-1",
                new BigDecimal("70000"),
                "ZAR",
                "ACME",
                "electronics",
                OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        Optional<RuleHit> hit = rule.evaluate(event);

        assertTrue(hit.isPresent());
        assertEquals("HIGH_AMOUNT", hit.get().ruleId());
        assertEquals("1.0", hit.get().ruleVersion());
        assertEquals(Severity.HIGH, hit.get().severity());
        assertNotNull(hit.get().metadata());
        assertEquals("50000", hit.get().metadata().get("threshold"));
        assertEquals("70000", hit.get().metadata().get("amount"));
    }

    @Test
    void whenAmountAtOrBelowThreshold_thenNoHit() {
        HighAmountRule rule = new HighAmountRule(new BigDecimal("50000"));

        TransactionEvent atThreshold = new TransactionEvent(
                "tx-2", "cust-1", new BigDecimal("50000"), "ZAR",
                "SHOPRITE", "groceries", OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        TransactionEvent belowThreshold = new TransactionEvent(
                "tx-3", "cust-1", new BigDecimal("49999.99"), "ZAR",
                "SHOPRITE", "groceries", OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        assertTrue(rule.evaluate(atThreshold).isEmpty());
        assertTrue(rule.evaluate(belowThreshold).isEmpty());
    }
}
