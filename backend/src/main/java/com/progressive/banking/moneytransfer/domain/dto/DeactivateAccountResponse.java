package com.progressive.banking.moneytransfer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeactivateAccountResponse {

    private Integer accountId;
    private String status;
    private String message;
}