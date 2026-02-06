package com.example.FraudRuleEngine.config;

import com.example.FraudRuleEngine.domain.rules.*;
import com.example.FraudRuleEngine.persistence.repo.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Configuration
public class RulesConfig {

     // --- Rule parameters (single source of truth) ---
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");

    private static final Set<String> MERCHANT_WATCHLIST = Set.of(
            "ACME",
            "BINANCE"
    );

    // Velocity: allow MAX_TX_PER_WINDOW transactions within WINDOW_MINUTES
    private static final int VELOCITY_WINDOW_MINUTES = 2;
    private static final int VELOCITY_MAX_TX_PER_WINDOW = 5;

    @Bean
    public List<FraudRule> fraudRules(TransactionRepository transactionRepository) {
        return List.of(
            new HighAmountRule(HIGH_AMOUNT_THRESHOLD),
            new MerchantWatchlistRule(MERCHANT_WATCHLIST),
            new VelocityRule(transactionRepository, VELOCITY_WINDOW_MINUTES, VELOCITY_MAX_TX_PER_WINDOW)
        );
    }

}
