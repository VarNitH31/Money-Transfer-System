package com.progressive.banking.moneytransfer.repository;

import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, String> {

    /**
     * Find transaction by idempotency key (for duplicate detection)
     */
    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    /**
     * Check if transaction exists with idempotency key
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find all transactions for a specific account (from or to)
     */
    @Query("SELECT t FROM TransactionLog t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId ORDER BY t.createdOn DESC")
    List<TransactionLog> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Find transactions by status
     */
    List<TransactionLog> findByStatus(TransactionStatus status);

    /**
     * Find transactions within date range
     */
    @Query("SELECT t FROM TransactionLog t WHERE t.createdOn BETWEEN :startDate AND :endDate ORDER BY t.createdOn DESC")
    List<TransactionLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count successful transactions
     */
    long countByStatus(TransactionStatus status);
}
