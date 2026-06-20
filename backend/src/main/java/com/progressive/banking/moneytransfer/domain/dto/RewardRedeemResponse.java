package com.progressive.banking.moneytransfer.domain.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RewardRedeemResponse {

    private Integer redeemedPoints;
    private BigDecimal redeemedAmount;
    private Integer remainingPoints;
    private BigDecimal newBalance;
}