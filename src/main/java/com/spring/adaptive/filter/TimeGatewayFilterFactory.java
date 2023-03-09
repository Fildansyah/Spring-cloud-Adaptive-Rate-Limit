package com.spring.adaptive.filter;

import com.spring.adaptive.model.RateLimiterConfig;
import com.spring.adaptive.service.RateLimiterService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
public class TimeGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimiterConfig> {
    private final RateLimiterService rateLimiterService;
    private final MeterRegistry meterRegistry;


    public TimeGatewayFilterFactory(RateLimiterService rateLimiterService, MeterRegistry meterRegistry) {
        super(RateLimiterConfig.class);
        this.rateLimiterService = rateLimiterService;
        this.meterRegistry = meterRegistry;
    }


    @Override
    public GatewayFilter apply(RateLimiterConfig config) {
       return new GatewayFilter() {
           @Override
           public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
               final long startTime = System.currentTimeMillis();
               return chain.filter(exchange).then(Mono.defer(()->{
                   meterRegistry.counter("api_gateway_request_success_count", "rateLimiter", config.getRateLimiter()).increment();
                   long endTime = System.currentTimeMillis();
                   long duration = endTime - startTime;
                   meterRegistry.timer("api_gateway_request_duration", "rateLimiter",config.getRateLimiter()).record(duration, TimeUnit.MILLISECONDS);
                    return rateLimiterService.increase(config.getRateLimiter(), duration);
               }));
           }
       };
    }

    public RateLimiterService getRateLimiterService(){
        return rateLimiterService;
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
