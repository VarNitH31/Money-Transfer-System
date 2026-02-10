package com.progressive.banking.moneytransfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.progressive.banking.moneytransfer.domain.dto.LoginRequest;
import com.progressive.banking.moneytransfer.domain.dto.LoginResponse;
import com.progressive.banking.moneytransfer.security.JwtUtil;
import com.progressive.banking.moneytransfer.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

    authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword())
        );
    
    	 String holderName=request.getUsername();
    	 String token=jwtUtil.generateToken(request.getUsername());
    	 Integer accountId=accountService.getAccountIdByHoldername(holderName);
    	

    	 return 
    	        new LoginResponse(token, holderName , accountId);
    	   
    }
}