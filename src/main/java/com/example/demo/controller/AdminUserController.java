package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Mask password hash for security
        List<Map<String, Object>> userDtos = users.stream().map(u -> Map.<String, Object>of(
                "userId", u.getUserId(),
                "email", u.getEmail(),
                "fullName", u.getFullName(),
                "role", u.getRole(),
                "createdAt", u.getCreatedAt()
        )).collect(Collectors.toList());
        
        return ResponseEntity.ok(userDtos);
    }

    @PutMapping("/{id}/role")
    @Transactional
    public ResponseEntity<?> updateUserRole(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        String newRole = request.get("role");
        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().body("Role is required");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        if (userRepository.existsByEmail(request.get("email"))) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        User user = new User();
        user.setFullName(request.get("fullName"));
        user.setEmail(request.get("email"));
        user.setUsername(request.get("email"));
        user.setRole(request.get("role") != null ? request.get("role") : "USER");
        
        String plainPassword = request.get("password");
        if (plainPassword == null || plainPassword.isEmpty()) plainPassword = "password123";
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        user.setFullName(request.get("fullName"));
        user.setRole(request.get("role"));
        // Email change might require more checks, but let's allow it for admin
        user.setEmail(request.get("email"));
        
        if (request.containsKey("password") && !request.get("password").isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.get("password")));
        }
        
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
