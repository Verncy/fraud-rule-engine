package com.example.FraudRuleEngine.persistence.repo;

import com.example.FraudRuleEngine.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    long countByCustomerIdAndEventTimeBetween(
            String customerId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    Optional<TransactionEntity> findByTransactionId(String transactionId);
}
