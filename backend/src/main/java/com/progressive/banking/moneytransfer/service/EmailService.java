package com.progressive.banking.moneytransfer.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("varnith3110@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Money Transfer System - Email Verification");

            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>

                        body {
                            margin: 0;
                            padding: 0;
                            background-color: #f4f6f8;
                            font-family: Arial, Helvetica, sans-serif;
                        }

                        .container {
                            max-width: 600px;
                            margin: 30px auto;
                            background-color: #ffffff;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                        }

                        .header {
                            background: linear-gradient(135deg, #2563eb, #1e40af);
                            color: white;
                            text-align: center;
                            padding: 30px;
                        }

                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                        }

                        .content {
                            padding: 30px;
                            color: #333333;
                            line-height: 1.6;
                        }

                        .otp-container {
                            text-align: center;
                            margin: 30px 0;
                        }

                        .otp {
                            display: inline-block;
                            background-color: #eff6ff;
                            color: #1e40af;
                            font-size: 34px;
                            font-weight: bold;
                            letter-spacing: 8px;
                            padding: 15px 30px;
                            border-radius: 10px;
                            border: 2px dashed #2563eb;
                        }

                        .info-box {
                            background-color: #f8fafc;
                            border-left: 4px solid #2563eb;
                            padding: 15px;
                            margin-top: 20px;
                            border-radius: 6px;
                        }

                        .footer {
                            background-color: #f8fafc;
                            text-align: center;
                            color: #6b7280;
                            padding: 20px;
                            font-size: 12px;
                        }

                    </style>
                </head>

                <body>

                    <div class="container">

                        <div class="header">
                            <h1>Money Transfer System</h1>
                            <p>Email Verification</p>
                        </div>

                        <div class="content">

                            <h2>Verify Your Email</h2>

                            <p>
                                Thank you for registering with
                                <strong>Money Transfer System</strong>.
                            </p>

                            <p>
                                Please use the following One-Time Password (OTP)
                                to complete your email verification:
                            </p>

                            <div class="otp-container">
                                <div class="otp">
                                    """ + otp + """
                                </div>
                            </div>

                            <div class="info-box">
                                <strong>Important:</strong>
                                This OTP is valid for 5 minutes.
                            </div>

                            <p>
                                If you did not request this verification,
                                you can safely ignore this email.
                            </p>

                        </div>

                        <div class="footer">
                            © 2026 Money Transfer System<br>
                            Secure Banking • Fast Transfers • Trusted Service
                        </div>

                    </div>

                </body>
                </html>
                """;

            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}