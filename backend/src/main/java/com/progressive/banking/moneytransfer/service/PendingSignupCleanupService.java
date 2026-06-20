package com.progressive.banking.moneytransfer.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progressive.banking.moneytransfer.repository.PendingSignupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PendingSignupCleanupService {

    private final PendingSignupRepository pendingSignupRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteExpiredPendingSignups() {

        pendingSignupRepository.deleteByExpiryTimeBefore(
                LocalDateTime.now());

    }
}