package com.spring.adaptive.runner;

import com.spring.adaptive.properties.RateLimiterMetricProperties;
import com.spring.adaptive.service.RateLimiterService;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterRunner implements ApplicationRunner {

    private final RateLimiterMetricProperties rateLimiterMetricProperties;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final MeterRegistry meterRegistry;
    private final RateLimiterService rateLimiterService;

    public RateLimiterRunner(RateLimiterMetricProperties rateLimiterMetricProperties, RateLimiterRegistry rateLimiterRegistry, MeterRegistry meterRegistry, RateLimiterService rateLimiterService) {
        this.rateLimiterMetricProperties = rateLimiterMetricProperties;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.meterRegistry = meterRegistry;
        this.rateLimiterService = rateLimiterService;
    }


    @Override
    public void run(ApplicationArguments args){
    registerRateLimiters();
    registerRateLimiterMetrics();

    }

    private void registerRateLimiters() {
        rateLimiterMetricProperties.getMetrics().forEach((name, metric) -> {
            RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitForPeriod(getMaxLimitForPeriod(name, metric.getMaxLimitForPeriod()))
                    .limitRefreshPeriod(metric.getLimitRefreshPeriod())
                    .build();
            rateLimiterRegistry.rateLimiter(name, config);
        });
    }

    private int getMaxLimitForPeriod(String name, int defaultLimit) {
        return rateLimiterService.getMaxLimit(name)
                .defaultIfEmpty(defaultLimit)
                .block();
    }

    private void registerRateLimiterMetrics(){
        rateLimiterMetricProperties.getMetrics().forEach((name, metric)->{
            Gauge.builder("resilience4j.ratelimiter.limit_for_period", rateLimiterRegistry, registry -> registry.rateLimiter(name).getRateLimiterConfig().getLimitForPeriod())
                    .description("The limit for the period")
                    .tag("rate_limiter", name)
                    .register(meterRegistry);
        });
    }


}
