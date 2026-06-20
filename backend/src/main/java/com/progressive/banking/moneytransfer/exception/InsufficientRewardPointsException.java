package com.progressive.banking.moneytransfer.exception;

public class InsufficientRewardPointsException
        extends RuntimeException {

    public InsufficientRewardPointsException(String message) {
        super(message);
    }
}