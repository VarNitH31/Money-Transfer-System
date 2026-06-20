package com.progressive.banking.moneytransfer.service.impl;

import java.math.BigDecimal;
import java.security.SecureRandom;

import com.progressive.banking.moneytransfer.domain.entities.PendingSignup;
import com.progressive.banking.moneytransfer.repository.PendingSignupRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progressive.banking.moneytransfer.domain.dto.LoginRequest;
import com.progressive.banking.moneytransfer.domain.dto.LoginResponse;
import com.progressive.banking.moneytransfer.domain.dto.SignupRequest;
import com.progressive.banking.moneytransfer.domain.dto.SignupResponse;
import com.progressive.banking.moneytransfer.domain.dto.VerifyOtpRequest;
import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.entities.User;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.repository.AccountRepository;
import com.progressive.banking.moneytransfer.repository.EmailOtpRepository;
import com.progressive.banking.moneytransfer.repository.UserRepository;
import com.progressive.banking.moneytransfer.security.JwtUtil;
import com.progressive.banking.moneytransfer.service.AccountService;
import com.progressive.banking.moneytransfer.service.AuthService;
import com.progressive.banking.moneytransfer.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PendingSignupRepository pendingSignupRepository;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    private final AccountService accountService;
    private final org.springframework.security.authentication.AuthenticationManager authManager;

    private final SecureRandom random = new SecureRandom();
    private final EmailOtpRepository emailOtpRepository;
    private final EmailService emailService;


    // ---------------- LOGIN ----------------
    @Override
    public LoginResponse login(LoginRequest request) {

        authManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword())
        );

            User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!user.isEmailVerified()) {
        throw new IllegalArgumentException(
                "Please verify your email before logging in");
    }

        String token = jwtUtil.generateToken(request.getUsername());

        Integer accountId =
                accountService.getAccountIdByHolderName(request.getUsername());

        return new LoginResponse(token, request.getUsername(), accountId);
    }

    // ---------------- SIGNUP ----------------
    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.findByUsername(request.getUserName()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (pendingSignupRepository
                .findByUsername(request.getUserName())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Username already awaiting verification"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }if (pendingSignupRepository
                .findByEmail(request.getEmail())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Email already awaiting verification"
            );
        }


// Remove any previous OTPs for this user

        String otp = generateOtp();

        PendingSignup pendingSignup = new PendingSignup();

        pendingSignup.setUsername(request.getUserName());

        pendingSignup.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        pendingSignup.setEmail(request.getEmail());

        pendingSignup.setOtp(otp);

        pendingSignup.setExpiryTime(
                java.time.LocalDateTime.now().plusMinutes(5)
        );

        pendingSignupRepository.save(pendingSignup);

        emailService.sendOtp(
                pendingSignup.getEmail(),
                otp
        );
        return new SignupResponse(
                "OTP sent successfully. Verify email to complete registration.",
                null
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
    
    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {

        PendingSignup pendingSignup = pendingSignupRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new IllegalArgumentException("OTP not found"));

        // OTP expired
        if (pendingSignup.getExpiryTime()
                .isBefore(java.time.LocalDateTime.now())) {

            pendingSignupRepository.delete(pendingSignup);

            throw new IllegalArgumentException("OTP expired");
        }

        // OTP incorrect
        if (!pendingSignup.getOtp()
                .equals(request.getOtp())) {

            throw new IllegalArgumentException("Invalid OTP");
        }

        // =========================
        // Create User
        // =========================

        User user = new User();

        user.setUsername(
                pendingSignup.getUsername());

        user.setPassword(
                pendingSignup.getPassword());

        user.setEmail(
                pendingSignup.getEmail());

        user.setEmailVerified(true);

        userRepository.save(user);

        // =========================
        // Create Account
        // =========================

        Account account = new Account();

        account.setAccountId(
                generateUniqueAccountNumber());

        account.setHolderName(
                pendingSignup.getUsername());

        account.setBalance(
                BigDecimal.valueOf(5000));

        account.setRewardPoints(0);

        account.setStatus(
                AccountStatusEnum.ACTIVE);

        accountRepository.save(account);

        pendingSignupRepository.delete(pendingSignup);
    }


}