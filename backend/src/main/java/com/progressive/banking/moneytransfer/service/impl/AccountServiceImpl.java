package com.progressive.banking.moneytransfer.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.progressive.banking.moneytransfer.domain.dto.*;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.exception.InsufficientRewardPointsException;
import com.progressive.banking.moneytransfer.exception.UnauthorizedAccountAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;
import com.progressive.banking.moneytransfer.domain.mapper.AccountMapper;
import com.progressive.banking.moneytransfer.domain.mapper.TransferMapper;
import com.progressive.banking.moneytransfer.exception.AccountNotFoundException;
import com.progressive.banking.moneytransfer.repository.AccountRepository;
import com.progressive.banking.moneytransfer.repository.TransactionLogRepository;
import com.progressive.banking.moneytransfer.service.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        return AccountMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        return AccountMapper.toBalanceResponse(account);
    }
    
    @Override
    public Integer getAccountIdByHolderName(String holderName) {
        return accountRepository.findByHolderName(holderName)
                .map(Account::getAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for user: " + holderName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> getTransactions(Integer id) {

        // validates account existence
        accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));

        List<TransactionLog> logs = transactionLogRepository
                .findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(id, id);

        return logs.stream().map(TransferMapper::toResponse).toList();
    }


    @Override
    @Transactional
    public RewardRedeemResponse redeemRewards(
            Integer accountId,
            String username) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found"));

        if (!account.getHolderName()
                .equalsIgnoreCase(username)) {

            throw new UnauthorizedAccountAccessException(
                    "Account does not belong to logged-in user");
        }

        int rewardPoints = account.getRewardPoints();

        if (rewardPoints < 10) {
            throw new InsufficientRewardPointsException(
                    "Minimum 10 reward points required");
        }



        BigDecimal redeemAmount = BigDecimal.valueOf(rewardPoints * 3L);

        account.setBalance(
                account.getBalance().add(redeemAmount));

        account.setRewardPoints(0);

        accountRepository.save(account);

        return new RewardRedeemResponse(
                rewardPoints,
                redeemAmount,
                0,
                account.getBalance()
        );
    }

    @Override
    @Transactional
    public DeactivateAccountResponse deactivateAccount(
            Integer accountId,
            String username) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found"));

        if (!account.getHolderName()
                .equalsIgnoreCase(username)) {

            throw new UnauthorizedAccountAccessException(
                    "Account does not belong to logged-in user");
        }

        if (account.getStatus() == AccountStatusEnum.LOCKED) {

            return new DeactivateAccountResponse(
                    accountId,
                    account.getStatus().name(),
                    "Account already deactivated"
            );
        }

        account.setStatus(AccountStatusEnum.LOCKED);

        accountRepository.save(account);

        return new DeactivateAccountResponse(
                accountId,
                account.getStatus().name(),
                "Account successfully deactivated"
        );
    }
}