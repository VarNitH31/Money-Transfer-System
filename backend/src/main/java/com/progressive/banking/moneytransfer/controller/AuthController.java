package com.progressive.banking.moneytransfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.progressive.banking.moneytransfer.domain.dto.LoginRequest;
import com.progressive.banking.moneytransfer.domain.dto.LoginResponse;
import com.progressive.banking.moneytransfer.domain.dto.SignupRequest;
import com.progressive.banking.moneytransfer.domain.dto.SignupResponse;
import com.progressive.banking.moneytransfer.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * LOGIN
     * Returns:
     *  - JWT token
     *  - username
     *  - accountId
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * SIGNUP
     * Creates:
     *  - new user
     *  - new account with ₹5000
     *  - unique 8-digit account number
     *
     * Returns:
     *  - account number
     *  - message (redirect to login)
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request) {

        SignupResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }
}