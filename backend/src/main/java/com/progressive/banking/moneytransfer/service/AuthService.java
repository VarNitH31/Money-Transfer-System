package com.progressive.banking.moneytransfer.service;

import com.progressive.banking.moneytransfer.domain.dto.LoginRequest;
import com.progressive.banking.moneytransfer.domain.dto.LoginResponse;
import com.progressive.banking.moneytransfer.domain.dto.SignupRequest;
import com.progressive.banking.moneytransfer.domain.dto.SignupResponse;

public interface AuthService {

    /**
     * Login user
     *  - validates username/password
     *  - generates JWT
     *  - returns token + accountId
     */
    LoginResponse login(LoginRequest request);

    /**
     * Signup user
     *  - validates username/password
     *  - creates User
     *  - creates Account with ₹5000 balance
     *  - generates unique 8-digit account number
     *  - returns account number + message
     */
    SignupResponse signup(SignupRequest request);
}