package com.crm.mcsv_auth.service;

public interface RateLimiterService {

    void checkRateLimit(String key, int limit, long windowSeconds);
}
