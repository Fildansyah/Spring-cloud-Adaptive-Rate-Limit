package com.spring.adaptive;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.Test;

public class RateLimiterTest {
    @Test
    void testRateLimiterSlow(){
        RateLimiterRegistry registry= RateLimiterRegistry.ofDefaults();
        RateLimiter rateLimiter = registry.rateLimiter("user");
        System.out.println(rateLimiter.getRateLimiterConfig().getLimitForPeriod());
        rateLimiter.changeLimitForPeriod(rateLimiter.getRateLimiterConfig().getLimitForPeriod() * 2);
        System.out.println(rateLimiter.getRateLimiterConfig().getLimitForPeriod());
    }

}
