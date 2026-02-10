package com.progressive.banking.moneytransfer.exception;
 
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;
 
import com.progressive.banking.moneytransfer.controller.AccountController;

import com.progressive.banking.moneytransfer.service.AccountService;
 
@WebMvcTest(AccountController.class)

@AutoConfigureMockMvc(addFilters = false)  // ✅ Added this

@Import(GlobalExceptionHandler.class)

class GlobalExceptionHandlerTest {
 
    @Autowired

    private MockMvc mockMvc;
 
    @MockitoBean

    private AccountService accountService;
 
    @Test

    @WithMockUser  // ✅ Added this in case Authentication is needed

    void accountNotFound_returns404AndErrorResponse() throws Exception {
 
        when(accountService.getAccount(999))

                .thenThrow(new AccountNotFoundException("Account not found: 999"));
 
        mockMvc.perform(get("/api/v1/accounts/999")

                .accept(MediaType.APPLICATION_JSON)

                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.errorCode").value("ACC-404"))

                .andExpect(jsonPath("$.message").value("Account not found: 999"));

    }
 
    @Test

    @WithMockUser  // ✅ Added this

    void accountNotActive_returns403AndErrorResponse() throws Exception {
 
        when(accountService.getBalance(1))

                .thenThrow(new AccountNotActiveException("Account is not active: 1"));
 
        mockMvc.perform(get("/api/v1/accounts/1/balance")

                .accept(MediaType.APPLICATION_JSON)

                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isForbidden())

                .andExpect(jsonPath("$.errorCode").value("ACC-403"));

    }
 
    @Test

    @WithMockUser  // ✅ Added this

    void insufficientBalance_returns400AndErrorResponse() throws Exception {
 
        when(accountService.getAccount(1))

                .thenThrow(new InsufficientBalanceException("Insufficient balance"));
 
        mockMvc.perform(get("/api/v1/accounts/1")

                .accept(MediaType.APPLICATION_JSON)

                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errorCode").value("TRX-400"));

    }

}
 