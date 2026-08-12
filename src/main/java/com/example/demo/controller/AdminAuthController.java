package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.MessageResponse;
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
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthService authService;
    private final RateLimitingService rateLimitingService;

    public AdminAuthController(AuthService authService, RateLimitingService rateLimitingService) {
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Bucket bucket = rateLimitingService.resolveBucket(getClientIP(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse("Too many requests"));
        }
        try {
            String[] tokens = authService.login(request, true);
            
            ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens[0])
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(900) // 15 mins
                    .build();
                    
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens[1])
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
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
}
