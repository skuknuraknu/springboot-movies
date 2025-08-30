package com.gugugaga.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gugugaga.gateway.service.UserPlanService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
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
    private final UserPlanService userPlanService;
    public RateLimiterFilter( UserPlanService userPlanService ) {
        this.userPlanService = userPlanService;
    }
    private static final Map<String, Bandwidth> SERVICE_LIMITS = Map.of(
        "auth",   Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))),
        "movies", Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))
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
        String userId = getCliendId(exchange);
        
        String key = userId + ":" + getServiceName(path);
        System.out.println(" - Set bucket key: " + key);
        return userPlanService.getUserRateLimit(userId).flatMap( res -> {
            String cacheKey = key;
            Bucket bucket = getBucket(cacheKey, res);
            // Try to consume 1 token (1 API request)
                if (bucket.tryConsume(1)) {
                    // Success: User has quota remaining
                    long remaining = bucket.getAvailableTokens();
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining", String.valueOf(remaining));
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Limit", String.valueOf(res));
                    System.out.println("Request allowed for user " + userId + " Remaining: " + remaining + "/" + res);
                    return chain.filter(exchange);
                } else {
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Limit", String.valueOf(res));
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining", "0");
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Seconds", "60");
                    
                    // Business Message: Encourage subscription upgrade
                    String upgradeMessage = res <= 10 ? 
                        "Rate limit exceeded. Upgrade to Premium for higher limits!" :
                        "Rate limit exceeded. Please try again in 1 minute.";
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Message", upgradeMessage);
                    return exchange.getResponse().setComplete();
                }
        });
    }

    private Bucket getBucket(String key, Long maxRequestsPerMinute) {
        return bucketCache.get(key, k -> {
            // Create bucket with user's subscription limit
            Bandwidth limit = Bandwidth.classic( maxRequestsPerMinute, 
                Refill.intervally(maxRequestsPerMinute, Duration.ofMinutes(1))
            );
            return Bucket.builder().addLimit(limit).build();
        });
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
    private String getCliendId(ServerWebExchange exchange ){
        List<String> userId = exchange.getRequest().getHeaders().get("X-User-Id");
        // Return the first value if present, otherwise fallback to IP
        if (userId != null && !userId.isEmpty()) {
            return userId.get(0);
        }
        return getClientIP(exchange); // Fallback IP
    }
    @Override
    public int getOrder() {
        return -100;  // Run this filter early (before other filters)
    }
}
