package com.progressive.banking.moneytransfer.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$",
            message = "Username must be 4-20 characters")
    private String username;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&+=]{6,}$",
        message = "Password must contain letters and numbers and be min 6 characters"
    )
    private String password;
}