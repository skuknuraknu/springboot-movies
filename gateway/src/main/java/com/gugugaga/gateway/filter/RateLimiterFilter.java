package com.gugugaga.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {
    private static final Map<String, Bandwidth> SERVICE_LIMITS = Map.of(
        "auth",   Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))),
        "movies", Bandwidth.classic(6, Refill.intervally(6, Duration.ofMinutes(1)))
    );

    // Remembers each user's bucket
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)  // Forget unused buckets after 1 hour
            .maximumSize(10000)  // Store max 10,000 buckets
            .build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String ip = getClientIP(exchange);
        
        String key = getServiceName(path) + ":" + ip;
        Bucket bucket = getBucket(key, getServiceName(path));
        System.out.println(" - Set bucket key: " + key);
        // Try to consume 1 token from the bucket
        if (bucket.tryConsume(1)) {
            // Success! User has tokens left, let the request through
            exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return chain.filter(exchange);
        } else {
            // No tokens left! Send back "Too Many Requests" error
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Seconds", "60");
            return exchange.getResponse().setComplete();
        }
    }

    private Bucket getBucket(String key, String service) {
        // Get existing bucket or create a new one
        return bucketCache.get( key, k -> createNewBucketForServices(service) );
    }
    private String getServiceName(String path) {
        // get only service name of the path (e.g.; /api/auth/login -> 'auth')
        String[] segments = path.split("/");
        System.out.println("🔍 Path segments: " + Arrays.toString(segments));
        if (segments.length > 2) {
            return segments[2]; // e.g.; /api/auth/login -> 'auth'
        } else if ( segments.length > 1) {
            return segments[1]; // e.g.; /api/auth -> 'auth'
        }
        return "unknown";
    }

    private Bucket createNewBucketForServices( String service ) {
        Bandwidth limit = SERVICE_LIMITS.getOrDefault(service, Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))) );
        return Bucket.builder().addLimit(limit).build();
    }
    private String getClientIP(ServerWebExchange exchange) {
        // Get the client's IP address
        String ip = exchange.getRequest().getRemoteAddress() != null ?  exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        return ip;
    }
    
    @Override
    public int getOrder() {
        return -100;  // Run this filter early (before other filters)
    }
}
