package com.progressive.banking.moneytransfer.repository;

import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account with pessimistic write lock for transaction safety
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    /**
     * Find account by holder name
     */
    Optional<Account> findByHolderName(String holderName);

    /**
     * Check if account exists with given status
     */
    boolean existsByIdAndStatus(Long id, AccountStatus status);

    /**
     * Count accounts by status
     */
    long countByStatus(AccountStatus status);
}