package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MerchantWatchlistRuleTest {

    @Test
    void whenMerchantOnWatchlist_thenReturnsRuleHit_caseInsensitive() {
        MerchantWatchlistRule rule = new MerchantWatchlistRule(Set.of("ACME", "BINANCE"));


        TransactionEvent event = new TransactionEvent(
                "tx-1",
                "cust-1",
                new BigDecimal("1000"),
                "ZAR",
                " acme ", // intentionally messy casing/spacing
                "electronics",
                OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        //Act
        Optional<RuleHit> hitOpt = rule.evaluate(event);

        //Assert
        assertTrue(hitOpt.isPresent());

        RuleHit hit = hitOpt.get();
        assertEquals("MERCHANT_WATCHLIST", hit.ruleId());
        assertEquals("1.0", hit.ruleVersion());
        assertEquals(Severity.MEDIUM, hit.severity());

        // Reason should include the original merchant string
        assertTrue(hit.reason().contains("acme"));

        assertNotNull(hit.metadata(), "metadata must not be null");

        // metadata key is "merchant"
        assertEquals(" acme ", hit.metadata().get("merchant")); // preserves original input

        assertEquals("ACME", hit.metadata().get("merchantNormalized"));
    }

    @Test
    void whenMerchantNotOnWatchlist_orNull_thenNoHit() {
        MerchantWatchlistRule rule = new MerchantWatchlistRule(Set.of("ACME", "BINANCE"));

        TransactionEvent notOnList = new TransactionEvent(
                "tx-2", "cust-1", new BigDecimal("100"), "ZAR",
                "PUMA", "clothing", OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        TransactionEvent nullMerchant = new TransactionEvent(
                "tx-3", "cust-1", new BigDecimal("100"), "ZAR",
                null, "clothing", OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        assertTrue(rule.evaluate(notOnList).isEmpty());
        assertTrue(rule.evaluate(nullMerchant).isEmpty());
    }
}
