package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;
import com.example.FraudRuleEngine.persistence.repo.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VelocityRuleTest {

    @Test
    void shouldTriggerVelocityWhenCountExceedsMax() {
        TransactionRepository repo = mock(TransactionRepository.class);

        int windowMinutes = 2;
        int maxCount = 5;

        // ✅ Correct constructor order: (repo, maxCount, windowMinutes)
        VelocityRule rule = new VelocityRule(repo, maxCount, windowMinutes);

        OffsetDateTime now = OffsetDateTime.parse("2026-02-01T10:00:00Z");

        TransactionEvent current = new TransactionEvent(
                "tx-1",
                "cust-123",
                new BigDecimal("100.00"),
                "ZAR",
                "ACME",
                "ECOM",
                now
        );

        // Return 6, which is > maxCount (5)
        when(repo.countByCustomerIdAndEventTimeBetween(eq("cust-123"), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(6L);

        Optional<RuleHit> hit = rule.evaluate(current);

        assertTrue(hit.isPresent(), "Expected velocity rule to trigger when count > maxCount");
        assertEquals("VELOCITY", hit.get().ruleId());
        assertEquals("1.0", hit.get().ruleVersion());
        assertEquals(Severity.HIGH, hit.get().severity());
        assertTrue(hit.get().reason().contains("Velocity threshold exceeded"));

        // Verify correct window calculation
        verify(repo, times(1)).countByCustomerIdAndEventTimeBetween(
                eq("cust-123"),
                eq(now.minusMinutes(windowMinutes)),
                eq(now)
        );
    }

    @Test
    void shouldNotTriggerVelocityWhenCountWithinLimit() {
        TransactionRepository repo = mock(TransactionRepository.class);

        int windowMinutes = 2;
        int maxCount = 5;

        VelocityRule rule = new VelocityRule(repo, maxCount, windowMinutes);

        OffsetDateTime now = OffsetDateTime.parse("2026-02-01T10:00:00Z");

        TransactionEvent current = new TransactionEvent(
                "tx-1",
                "cust-123",
                new BigDecimal("100.00"),
                "ZAR",
                "ACME",
                "ECOM",
                now
        );

        // Return 5, which is NOT > maxCount (5), so should NOT trigger
        when(repo.countByCustomerIdAndEventTimeBetween(eq("cust-123"), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(5L);

        Optional<RuleHit> hit = rule.evaluate(current);

        assertTrue(hit.isEmpty(), "Expected velocity rule NOT to trigger when count <= maxCount");

        verify(repo, times(1)).countByCustomerIdAndEventTimeBetween(
                eq("cust-123"),
                eq(now.minusMinutes(windowMinutes)),
                eq(now)
        );
    }
}
