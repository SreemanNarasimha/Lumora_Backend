package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ResendConfig {

    @Bean
    public WebClient resendWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.resend.com")
            .build();
    }
}
