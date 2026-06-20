package com.progressive.banking.moneytransfer.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.progressive.banking.moneytransfer.service.RewardService;

@Service
public class RewardServiceImpl implements RewardService {

    @Override
    public int calculateRewardPoints(BigDecimal transferAmount) {

        return transferAmount.intValue() / 100;
    }
}