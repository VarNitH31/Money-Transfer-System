package com.progressive.banking.moneytransfer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.progressive.banking.moneytransfer.domain.entities.PendingSignup;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PendingSignupRepository
        extends JpaRepository<PendingSignup, Long> {

    Optional<PendingSignup> findByUsername(String username);

    Optional<PendingSignup> findByEmail(String email);

    void deleteByUsername(String username);

    @Modifying
    @Transactional
    void deleteByExpiryTimeBefore(LocalDateTime time);
}