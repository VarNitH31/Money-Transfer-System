package com.progressive.banking.moneytransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class MoneytransferApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneytransferApplication.class, args);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "password";   // change this
        String encoded = encoder.encode(rawPassword);

        System.out.println("Encoded password:");
        System.out.println(encoded);
	}

}