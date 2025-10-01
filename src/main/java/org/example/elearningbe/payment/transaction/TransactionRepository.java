package org.example.elearningbe.payment.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByAppTransId(String appTransId);

    Optional<Transaction> findByRefundId(String refundId);

    Optional<Transaction> findByZpTransId(String zpTransId);
}

