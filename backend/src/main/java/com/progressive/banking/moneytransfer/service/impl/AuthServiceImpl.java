package com.progressive.banking.moneytransfer.service.impl;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progressive.banking.moneytransfer.domain.dto.LoginRequest;
import com.progressive.banking.moneytransfer.domain.dto.LoginResponse;
import com.progressive.banking.moneytransfer.domain.dto.SignupRequest;
import com.progressive.banking.moneytransfer.domain.dto.SignupResponse;
import com.progressive.banking.moneytransfer.domain.dto.VerifyOtpRequest;
import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.entities.EmailOtp;
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

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEmailVerified(false);

        userRepository.save(user);

        // Create account
        Account account = new Account();
        account.setAccountId(generateUniqueAccountNumber());
        account.setHolderName(request.getUserName());
        account.setBalance(BigDecimal.valueOf(5000));
        account.setStatus(AccountStatusEnum.ACTIVE);

        accountRepository.save(account);

        // Generate OTP
        String otp = generateOtp();

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUsername(user.getUsername());
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(java.time.LocalDateTime.now().plusMinutes(5));

        emailOtpRepository.save(emailOtp);

        // Send Email
        emailService.sendOtp(user.getEmail(), otp);

        return new SignupResponse(
                "Signup successful. OTP sent to your email.",
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
    
    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {

        EmailOtp emailOtp = emailOtpRepository
                .findTopByUsernameOrderByExpiryTimeDesc(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("OTP not found"));

        if (emailOtp.getExpiryTime().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        if (!emailOtp.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);
    }


}