package com.progressive.banking.moneytransfer.exception;
public class UnauthorizedAccountAccessException extends RuntimeException {
    public UnauthorizedAccountAccessException(String msg) {
        super(msg);
    }
}