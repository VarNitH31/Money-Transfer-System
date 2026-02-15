package com.progressive.banking.moneytransfer.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String otp;
}
