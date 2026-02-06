package com.example.FraudRuleEngine.domain.rules;

import com.example.FraudRuleEngine.domain.model.TransactionEvent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MerchantWatchlistRule implements FraudRule {

    private final Set<String> watchlist;

    public MerchantWatchlistRule(Set<String> watchlist) {
        this.watchlist = watchlist;
    }

    @Override
    public String id() {
        return "MERCHANT_WATCHLIST";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public Severity severity() {
        return Severity.MEDIUM;
    }

    @Override
    public Optional<RuleHit> evaluate(TransactionEvent event) {
        if (event == null || event.merchant() == null) return Optional.empty();

        String original = event.merchant();
        String normalized = original.trim().toUpperCase();

        if (!watchlist.contains(normalized)) return Optional.empty();

        return Optional.of(new RuleHit(
                id(),
                version(),
                severity(),
                "Merchant is on watchlist: " + original,
                Map.of(
                        "merchant", original,
                        "merchantNormalized", normalized
                )
        ));
    }
}
