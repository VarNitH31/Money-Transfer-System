package com.progressive.banking.moneytransfer.service.impl;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progressive.banking.moneytransfer.domain.dto.*;
import com.progressive.banking.moneytransfer.domain.entities.*;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.exception.AccountNotFoundException;
import com.progressive.banking.moneytransfer.repository.AccountRepository;
import com.progressive.banking.moneytransfer.repository.UserRepository;
import com.progressive.banking.moneytransfer.security.JwtUtil;
import com.progressive.banking.moneytransfer.service.AccountService;
import com.progressive.banking.moneytransfer.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    private final AccountService accountService;
    private final org.springframework.security.authentication.AuthenticationManager authManager;

    private final SecureRandom random = new SecureRandom();

    // ---------------- LOGIN ----------------
    @Override
    public LoginResponse login(LoginRequest request) {

        authManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getUsername());

        Integer accountId =
                accountService.getAccountIdByHolderName(request.getUsername());

        return new LoginResponse(token, request.getUsername(), accountId);
    }

    // ---------------- SIGNUP ----------------
    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // create account
        Account account = new Account();
        account.setAccountId(generateUniqueAccountNumber());
        account.setHolderName(request.getUsername());
        account.setBalance(BigDecimal.valueOf(5000));
        account.setStatus(AccountStatusEnum.ACTIVE);

        accountRepository.save(account);

        return new SignupResponse(
                "Signup successful. Please login.",
                account.getAccountId()
        );
    }

    // ---------------- ACCOUNT NUMBER GENERATOR ----------------
    private Integer generateUniqueAccountNumber() {
        int number;
        do {
            number = 10000000 + random.nextInt(90000000); // 8 digit
        } while (accountRepository.existsById(number));

        return number;
    }
}