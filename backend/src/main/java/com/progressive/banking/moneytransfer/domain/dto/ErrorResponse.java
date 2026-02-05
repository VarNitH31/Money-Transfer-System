package com.progressive.banking.moneytransfer.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;

    private int status;  // HTTP status code (e.g., 400, 404, 500)

    private String error;  // Error description (e.g., "Bad Request")

    private String message; // Custom message

    private String path; // Request URI
}