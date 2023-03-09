package com.spring.adaptive.filter;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class ExceptionGlobalFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(RequestNotPermitted.class, throwable -> {
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Error", "true");
                    exchange.getResponse().getHeaders().add("Content-Type", "text/html");

                    byte[] errorResponse = "Too Many Request".getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorResponse);

                    return exchange.getResponse().writeWith(Mono.just(buffer));
                });
    }
}
