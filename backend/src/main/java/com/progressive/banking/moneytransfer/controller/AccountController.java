package com.progressive.banking.moneytransfer.controller;

import java.util.List;

import com.progressive.banking.moneytransfer.domain.dto.*;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.progressive.banking.moneytransfer.service.AccountService;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    /**
     * GET /api/v1/accounts/{id} -> Get account details
     */
    @GetMapping("/{id}")
    // Optional method-security example:
    // @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable("id") @Min(value = 1, message = "id must be >= 1") Integer id,
            Authentication authentication) {

        AccountResponse response = accountService.getAccount(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/accounts/{id}/balance -> Get account balance
     */
    @GetMapping("/{id}/balance")
    // Optional:
    // @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable("id") @Min(value = 1, message = "id must be >= 1") Integer id,
            Authentication authentication) {

        BalanceResponse response = accountService.getBalance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/accounts/{id}/transactions -> Get transaction history
     */
    @GetMapping("/{id}/transactions")
    // Optional:
    // @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TransferResponse>> getTransactions(
            @PathVariable("id") @Min(value = 1, message = "id must be >= 1") Integer id,
            Authentication authentication) {

        List<TransferResponse> response = accountService.getTransactions(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/redeem-rewards")
    public ResponseEntity<RewardRedeemResponse> redeemRewards(
            @PathVariable Integer id,
            Authentication authentication) {

        RewardRedeemResponse response =
                accountService.redeemRewards(
                        id,
                        authentication.getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<DeactivateAccountResponse>
    deactivateAccount(
            @PathVariable Integer id,
            Authentication authentication) {

        DeactivateAccountResponse response =
                accountService.deactivateAccount(
                        id,
                        authentication.getName());

        return ResponseEntity.ok(response);
    }
}