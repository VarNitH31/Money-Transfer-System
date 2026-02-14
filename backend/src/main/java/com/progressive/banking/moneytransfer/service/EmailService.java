package com.progressive.banking.moneytransfer.service;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
	
	
    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Money Transfer System - Email Verification");
        message.setText(
                "Your OTP is: " + otp +
                "\nValid for 5 minutes."
        );

        mailSender.send(message);
    }
}
