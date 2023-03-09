package com.spring.adaptive.service;

import com.spring.adaptive.exception.NoRequestException;
import com.spring.adaptive.properties.RateLimiterMetricProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RateLimiterService implements InitializingBean {

    private final RateLimiterMetricProperties rateLimiterMetricProperties;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    public RateLimiterService(RateLimiterMetricProperties rateLimiterMetricProperties,
                              ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        this.rateLimiterMetricProperties = rateLimiterMetricProperties;
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        rateLimiterMetricProperties.getMetrics().forEach((key, value) -> {
            Mono.zip(
                    reactiveStringRedisTemplate.opsForValue().setIfAbsent(key + "_duration", "0"),
                    reactiveStringRedisTemplate.opsForValue().setIfAbsent(key + "_request", "0"),
                    reactiveStringRedisTemplate.opsForValue().setIfAbsent(key + "_max_limit", String.valueOf(value.getMaxLimitForPeriod()))
            ).block();
        });
    }

    public Mono<Void> increase(String key, long duration) {
        return Mono.zip(
                reactiveStringRedisTemplate.opsForValue().increment(key + "_duration", duration),
                reactiveStringRedisTemplate.opsForValue().increment(key + "_request", 1)
        ).then();
    }

    public Mono<Void> reset(String key) {
        return Mono.zip(
                reactiveStringRedisTemplate.opsForValue().set(key + "_duration", "0"),
                reactiveStringRedisTemplate.opsForValue().set(key + "_request", "0")
        ).then();
    }

    public Mono<Boolean> isSlow(String key) {
        return Mono.zip(
                reactiveStringRedisTemplate.opsForValue().get(key + "_duration"),
                reactiveStringRedisTemplate.opsForValue().get(key + "_request")
        ).map(it -> {
            long duration = Long.parseLong(it.getT1());
            long request = Long.parseLong(it.getT2());
            long slowDuration = rateLimiterMetricProperties.getMetrics().get(key).getSlowDuration().toMillis();

            if (request == 0L) {
                throw new NoRequestException("No request for rate limiter " + key);
            } else {
                return duration / request > slowDuration;
            }
        });
    }

    public Mono<Integer> changeMaxLimit(String key, int maxLimit) {
        return reactiveStringRedisTemplate.opsForValue().set(key + "_max_limit", String.valueOf(maxLimit))
                .thenReturn(maxLimit);
    }

    public Mono<Integer> getMaxLimit(String key) {
        return reactiveStringRedisTemplate.opsForValue().get(key + "_max_limit")
                .map(Integer::parseInt);
    }

}
