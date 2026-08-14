package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class MailConnectionVerifier implements CommandLineRunner {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Override
    public void run(String... args) {
        try {
            mailSender.testConnection();
            System.out.println("SMTP CONNECTION SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
