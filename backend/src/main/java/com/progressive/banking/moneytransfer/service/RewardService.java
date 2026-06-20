package com.progressive.banking.moneytransfer.service;

import java.math.BigDecimal;

public interface RewardService {

    int calculateRewardPoints(BigDecimal transferAmount);

}