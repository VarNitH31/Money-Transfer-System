package com.progressive.banking.moneytransfer.domain.mapper;

import com.progressive.banking.moneytransfer.domain.dto.AccountResponse;
import com.progressive.banking.moneytransfer.domain.entities.Account;

public class AccountMapper {

    private AccountMapper() {}

    public static AccountResponse toResponse(Account account) {
        if (account == null) return null;

        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .holderName(account.getHolderName())
                .balance(account.getBalance())
                .status(account.getStatus())
                .version(account.getVersion())
                .lastUpdated(account.getLastUpdated())
                .build();
    }
}