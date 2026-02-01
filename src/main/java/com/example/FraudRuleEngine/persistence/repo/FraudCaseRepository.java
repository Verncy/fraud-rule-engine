package com.example.FraudRuleEngine.persistence.repo;

import com.example.FraudRuleEngine.persistence.entity.FraudCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FraudCaseRepository extends JpaRepository<FraudCaseEntity, UUID> {
    Optional<FraudCaseEntity> findByTransaction_TransactionId(String transactionId);
}
