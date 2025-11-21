package com.example.gateway.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpRateLimiterFilter implements GlobalFilter, Ordered {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private final long capacity;
    private final long refillTokens;
    private final Duration refillDuration;

    public IpRateLimiterFilter(
            @Value("${rate-limit.capacity:20}") long capacity,
            @Value("${rate-limit.refill-tokens:20}") long refillTokens,
            @Value("${rate-limit.refill-duration:1s}") Duration refillDuration) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = refillDuration;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String ip = resolveClientIp(exchange.getRequest());
        TokenBucket bucket = buckets.computeIfAbsent(ip, k -> new TokenBucket(capacity, refillTokens, refillDuration));

        if (bucket.tryConsume()) {
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(bucket.getTokens()));
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(capacity));
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(refillDuration.toSeconds()));
            return exchange.getResponse().setComplete();
        }
    }

    private String resolveClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        String xff = headers.getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }

        String realIp = headers.getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        InetSocketAddress remote = request.getRemoteAddress();
        if (remote == null) {
            return "unknown";
        }

        InetAddress addr = remote.getAddress();
        if (addr != null) {
            return addr.getHostAddress();
        }

        String host = remote.getHostString();
        return (host != null && !host.isBlank()) ? host : "unknown";
    }

    @Override
    public int getOrder() {
        return -100; // early in chain
    }

    static class TokenBucket {
        private final long capacity;
        private final long refillTokens;
        private final long refillNanos;
        private long tokens;
        private long lastRefillNanos;

        TokenBucket(long capacity, long refillTokens, Duration refillDuration) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillNanos = refillDuration.toNanos();
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        synchronized long getTokens() {
            refill();
            return tokens;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed >= refillNanos) {
                long periods = elapsed / refillNanos;
                long add = periods * refillTokens;
                tokens = Math.min(capacity, tokens + add);
                lastRefillNanos += periods * refillNanos;
            }
        }
    }
}
