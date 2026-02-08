package com.crm.mcsv_auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static class RateLimitState {
        long windowStartMillis;
        int count;
    }

    private final ConcurrentHashMap<String, RateLimitState> states = new ConcurrentHashMap<>();

    public void checkRateLimit(String key, int limit, long windowSeconds) {
        long now = System.currentTimeMillis();
        RateLimitState state = states.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStartMillis >= windowSeconds * 1000) {
                RateLimitState fresh = new RateLimitState();
                fresh.windowStartMillis = now;
                fresh.count = 1;
                return fresh;
            }
            existing.count += 1;
            return existing;
        });

        if (state.count > limit) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
        }
    }
}
