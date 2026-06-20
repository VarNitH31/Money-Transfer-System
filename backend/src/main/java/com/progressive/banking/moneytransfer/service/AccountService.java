package com.progressive.banking.moneytransfer.service;

import java.util.List;

import com.progressive.banking.moneytransfer.domain.dto.*;

public interface AccountService {

    AccountResponse getAccount(Integer id);

    BalanceResponse getBalance(Integer id);

    List<TransferResponse> getTransactions(Integer id);
    
    Integer getAccountIdByHolderName(String holderName);

    RewardRedeemResponse redeemRewards(
            Integer accountId,
            String username);

    DeactivateAccountResponse deactivateAccount(
            Integer accountId,
            String username);
}