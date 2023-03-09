package com.spring.adaptive.filter;


import com.spring.adaptive.model.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;


@Component
public class RateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimiterConfig> {
    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimiterGatewayFilterFactory(RateLimiterRegistry rateLimiterRegistry) {
        super(RateLimiterConfig.class);
        this.rateLimiterRegistry = rateLimiterRegistry;
    }


    @Override
    public GatewayFilter apply(RateLimiterConfig config) {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                    .transformDeferred(RateLimiterOperator.of(rateLimiterRegistry.rateLimiter(config.getRateLimiter())));
        };
    }

}
