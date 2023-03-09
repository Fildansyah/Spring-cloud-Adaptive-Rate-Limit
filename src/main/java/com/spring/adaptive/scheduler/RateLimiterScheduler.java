package com.spring.adaptive.scheduler;

import com.spring.adaptive.exception.NoRequestException;
import com.spring.adaptive.properties.RateLimiterMetricProperties;
import com.spring.adaptive.service.RateLimiterService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RateLimiterScheduler {

    private RateLimiterRegistry rateLimiterRegistry;
    private RateLimiterService rateLimiterService;
    private RateLimiterMetricProperties rateLimiterMetricProperties;
    private final MeterRegistry meterRegistry;



    @Autowired
    public RateLimiterScheduler(
            RateLimiterRegistry rateLimiterRegistry,
            RateLimiterService rateLimiterService,
            RateLimiterMetricProperties rateLimiterMetricProperties, MeterRegistry meterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.rateLimiterService = rateLimiterService;
        this.rateLimiterMetricProperties = rateLimiterMetricProperties;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(cron = "*/60 * * * * *")
    public void increaseRateLimiter() {
        rateLimiterMetricProperties.getMetrics().forEach((name, metric) -> {
            Mono.zip(
                            rateLimiterService.isSlow(name),
                            rateLimiterService.getMaxLimit(name)
                    )
                    .flatMap(tuple -> changeMaxLimit(tuple.getT1(), tuple.getT2(), metric, name))
                    .doOnSuccess(newLimit -> changeRateLimiterConfig(name, newLimit))
                    .doOnError(NoRequestException.class, e -> System.out.println("No request, do nothing"))
                    .then(rateLimiterService.reset(name))
                    .subscribe(
                            success -> System.out.println("Success run scheduler"),
                            error -> System.out.println("Status: " + error.getMessage())
                    );
        });
    }

    private void changeRateLimiterConfig(String name, int newLimit) {
        RateLimiter rateLimit = rateLimiterRegistry.rateLimiter(name);
        if (newLimit != rateLimit.getRateLimiterConfig().getLimitForPeriod()) {
            System.out.println("change the limit to " + newLimit);
            rateLimit.changeLimitForPeriod(newLimit);
        }
    }

    private Mono<Integer> changeMaxLimit(
            boolean isSlow,
            int maxLimit,
            RateLimiterMetricProperties.RateLimiterMetricDetailProperties metric,
            String name
    ) {
        if (isSlow) {
            System.out.println("is slow");
            return decreaseLimit(maxLimit, metric, name);
        } else {
            System.out.println("is not slow");
            return increaseLimit(maxLimit, metric, name);
        }
    }

    private Mono<Integer> increaseLimit(
            int maxLimit,
            RateLimiterMetricProperties.RateLimiterMetricDetailProperties metric,
            String name
    ) {
        if (maxLimit >= metric.getMaxLimitForPeriod()) {
            return rateLimiterService.changeMaxLimit(name, metric.getMaxLimitForPeriod());
        } else {
            return rateLimiterService.changeMaxLimit(name, maxLimit * 2);
        }
    }

    private Mono<Integer> decreaseLimit(
            int maxLimit,
            RateLimiterMetricProperties.RateLimiterMetricDetailProperties metric,
            String name
    ) {
        int newMaxLimit = maxLimit / 2;
        if (newMaxLimit < metric.getMinLimitForPeriod()) {
            return rateLimiterService.changeMaxLimit(name, metric.getMinLimitForPeriod());
        } else {
            return rateLimiterService.changeMaxLimit(name, newMaxLimit);
        }
    }
}
