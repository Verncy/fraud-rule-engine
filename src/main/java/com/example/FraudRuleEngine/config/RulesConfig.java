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

    @Bean
    public List<FraudRule> fraudRules(TransactionRepository transactionRepository) {
        return List.of(
            new HighAmountRule(new BigDecimal("50000")),
            new MerchantWatchlistRule(Set.of("ACME", "BINANCE")),
            new VelocityRule(transactionRepository, 10,3)
        );
    }

}
