package com.progressive.banking.moneytransfer.repository;

import com.progressive.banking.moneytransfer.domain.entities.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository
        extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByUsernameOrderByExpiryTimeDesc(String username);
}
