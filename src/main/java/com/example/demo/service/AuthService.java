package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.PendingRegistration;
import com.example.demo.entity.User;
import com.example.demo.entity.JwtToken;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.repository.PendingRegistrationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.JwtTokenRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResendEmailService emailService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PendingRegistrationRepository pendingRegistrationRepository,
                       JwtTokenRepository jwtTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder, ResendEmailService emailService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public MessageResponse sendOtp(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already registered. Please login.");
        }

        PendingRegistration pending = pendingRegistrationRepository.findByEmail(email)
                .orElse(new PendingRegistration());

        if (pending.getId() != null && pending.getCreatedAt() != null) {
            // Check cooldown (60 seconds)
            // Note: PendingRegistration uses createdAt/lastSentAt implicitly. We didn't add lastSentAt explicitly in PendingRegistration,
            // but we can just update createdAt or use a simple check on otpExpiresAt.
            // Since otpExpiresAt was 15m, now we make it 5m.
            if (pending.getOtpExpiresAt() != null && pending.getOtpExpiresAt().minusMinutes(5).plusSeconds(60).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Please wait 60 seconds before requesting a new OTP.");
            }
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        String otpHash = passwordEncoder.encode(otp);

        pending.setEmail(email);
        pending.setFullName("Pending");
        pending.setUsername("pending");
        pending.setPasswordHash("");
        pending.setOtpHash(otpHash);
        pending.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));
        pending.setAttemptCount(0);
        pending.setIsEmailVerified(false);

        pendingRegistrationRepository.save(pending);

        emailService.sendOtpEmail(email, otp);

        return new MessageResponse("OTP sent to " + email);
    }

    @Transactional
    public MessageResponse verifyEmail(VerifyOtpRequest request) {
        PendingRegistration pending = pendingRegistrationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No pending registration found for this email."));

        if (pending.getAttemptCount() >= 5) {
            throw new RuntimeException("Too many failed attempts. Please request a new OTP.");
        }

        if (pending.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(request.getOtp(), pending.getOtpHash())) {
            pending.setAttemptCount(pending.getAttemptCount() + 1);
            pendingRegistrationRepository.save(pending);
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        pending.setIsEmailVerified(true);
        pendingRegistrationRepository.save(pending);

        return new MessageResponse("Email verified successfully.");
    }

    @Transactional
    public MessageResponse completeRegistration(RegisterRequest request) {
        PendingRegistration pending = pendingRegistrationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not verified. Please verify your email first."));

        if (!Boolean.TRUE.equals(pending.getIsEmailVerified())) {
            throw new RuntimeException("Email not verified. Please verify your email first.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken.");
        }

        User user = new User();
        user.setEmail(pending.getEmail());
        user.setPhone(request.getPhone());
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setIsVerified(true);
        userRepository.save(user);

        pendingRegistrationRepository.delete(pending);

        return new MessageResponse("Registration successful! You can now login.");
    }

    @Transactional
    public String[] login(LoginRequest request, boolean isAdminLogin) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Invalid credentials")));
                
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        boolean isUserAdmin = !user.getRole().equals("USER");
        if (isAdminLogin && !isUserAdmin) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }
        
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        JwtToken jwtToken = new JwtToken();
        jwtToken.setUser(user);
        jwtToken.setRefreshTokenHash(hashRefreshToken(refreshToken));
        jwtToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        jwtToken.setRevoked(false);
        jwtTokenRepository.save(jwtToken);
        
        return new String[]{accessToken, refreshToken};
    }

    @Transactional
    public MessageResponse forgotPasswordSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user).ifPresent(token -> {
            if (token.getLastSentAt() != null && token.getLastSentAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Please wait 60 seconds before requesting a new OTP.");
            }
        });

        // Invalidate all previous tokens
        passwordResetTokenRepository.invalidateAllUserTokens(user);

        String otp = String.format("%06d", new Random().nextInt(999999));
        String otpHash = passwordEncoder.encode(otp);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setOtpHash(otpHash);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        resetToken.setIsVerified(false);
        resetToken.setAttemptCount(0);
        resetToken.setUsed(false);
        resetToken.setLastSentAt(LocalDateTime.now());

        passwordResetTokenRepository.save(resetToken);
        emailService.sendOtpEmail(email, otp);

        return new MessageResponse("OTP sent to " + email);
    }

    @Transactional
    public MessageResponse forgotPasswordVerifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken resetToken = passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("No password reset request found."));

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new RuntimeException("This OTP has already been used or invalidated.");
        }

        if (resetToken.getAttemptCount() >= 5) {
            throw new RuntimeException("Too many failed attempts. Please request a new OTP.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(request.getOtp(), resetToken.getOtpHash())) {
            resetToken.setAttemptCount(resetToken.getAttemptCount() + 1);
            passwordResetTokenRepository.save(resetToken);
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        resetToken.setIsVerified(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("OTP verified successfully. You can now reset your password.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken resetToken = passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("No password reset request found."));

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new RuntimeException("This OTP has already been used.");
        }

        if (!Boolean.TRUE.equals(resetToken.getIsVerified())) {
            throw new RuntimeException("OTP not verified. Please verify OTP first.");
        }

        if (!passwordEncoder.matches(request.getOtp(), resetToken.getOtpHash())) {
            throw new RuntimeException("Invalid OTP.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("Password reset successfully. You can now login.");
    }

    private String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }
}

