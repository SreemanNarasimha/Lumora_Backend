package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.AuthService;
import com.example.demo.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RateLimitingService rateLimitingService;

    public AuthController(AuthService authService, RateLimitingService rateLimitingService) {
        this.authService = authService;
        this.rateLimitingService = rateLimitingService;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request, HttpServletRequest httpRequest) {
        Bucket bucket = rateLimitingService.resolveBucket(getClientIP(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many requests. Please try again later."));
        }
        try {
            return ResponseEntity.ok(authService.sendOtp(request.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            return ResponseEntity.ok(authService.verifyEmail(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        Bucket bucket = rateLimitingService.resolveBucket(getClientIP(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many requests. Please try again later."));
        }
        try {
            return ResponseEntity.ok(authService.completeRegistration(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Bucket bucket = rateLimitingService.resolveBucket(getClientIP(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many requests"));
        }
        try {
            String[] tokens = authService.login(request, false);
            
            ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens[0])
                    .httpOnly(true)
                    .secure(httpRequest.isSecure())
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(900) // 15 mins
                    .build();
                    
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens[1])
                    .httpOnly(true)
                    .secure(httpRequest.isSecure())
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
                    .maxAge(7 * 24 * 3600) // 7 days
                    .build();
                    
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(new MessageResponse("Logged in successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> forgotPasswordSendOtp(@Valid @RequestBody SendOtpRequest request, HttpServletRequest httpRequest) {
        Bucket bucket = rateLimitingService.resolveBucket(getClientIP(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many requests. Please try again later."));
        }
        try {
            return ResponseEntity.ok(authService.forgotPasswordSendOtp(request.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> forgotPasswordVerifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            return ResponseEntity.ok(authService.forgotPasswordVerifyOtp(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            return ResponseEntity.ok(authService.resetPassword(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(httpRequest.isSecure())
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
                
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(httpRequest.isSecure())
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new MessageResponse("Logged out successfully"));
    }
}
