package com.example.demo.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private final Map<String, Bucket> otpEmailCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> otpIpCache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, this::newBucket);
    }

    public Bucket resolveOtpEmailBucket(String email) {
        return otpEmailCache.computeIfAbsent(email, k -> {
            Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(10)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    public Bucket resolveOtpIpBucket(String ip) {
        return otpIpCache.computeIfAbsent(ip, k -> {
            Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofHours(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
