package com.spring.adaptive.model;

public class RateLimiterConfig {
    private String rateLimiter = "default";

    public String getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(String rateLimiter) {
        this.rateLimiter = rateLimiter;
    }
}
