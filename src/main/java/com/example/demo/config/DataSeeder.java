package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String testEmail = "testuser@example.com";
            User user = userRepository.findByEmail(testEmail).orElse(null);
            
            if (user == null) {
                user = new User();
                user.setEmail(testEmail);
                user.setUsername("testuser");
                user.setFullName("Test User");
                user.setRole("USER");
                user.setIsVerified(true);
            }
            
            // Force reset password to password123 so the user can always log in
            user.setPasswordHash(passwordEncoder.encode("password123"));
            userRepository.save(user);
        };
    }
}
