package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@lumora.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("Super Admin");
            admin.setEmail(adminEmail);
            String defaultPassword = System.getenv("ADMIN_PASSWORD");
            if (defaultPassword == null || defaultPassword.trim().isEmpty()) {
                defaultPassword = java.util.UUID.randomUUID().toString();
                System.out.println("=================================================");
                System.out.println("GENERATED ADMIN PASSWORD: " + defaultPassword);
                System.out.println("=================================================");
            }
            admin.setPasswordHash(passwordEncoder.encode(defaultPassword)); // Dynamic password
            admin.setPhone("1234567890");
            admin.setRole("SUPER_ADMIN");
            admin.setIsVerified(true);
            
            userRepository.save(admin);
            System.out.println("Super Admin user created automatically!");
        }
    }
}
