package com.gugugaga.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gugugaga.gateway.config.ServiceConfiguration;
import com.gugugaga.gateway.service.UserPlanService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(RateLimiterFilter.class);
    private final Cache<String, Bucket> bucketCache;
    private final UserPlanService userPlanService;
    private final ServiceConfiguration serviceConfig;
    
    public RateLimiterFilter(UserPlanService userPlanService, ServiceConfiguration serviceConfig) {
        this.userPlanService = userPlanService;
        this.serviceConfig = serviceConfig;
            // Remembers each user's bucket
        this.bucketCache = Caffeine.newBuilder()
                .expireAfterWrite(serviceConfig.getRateLimiting().getCache().getExpireAfterWriteHours(), TimeUnit.HOURS)
                .maximumSize(serviceConfig.getRateLimiting().getCache().getMaximumSize())
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String ip = getClientIP(exchange);
        String userId = getCliendId(exchange);
        
        // INFO: Log each request being processed
        log.info("🔍 Processing user request - User: {}, Service: {}, Path: {}, IP: {}", 
                exchange.getRequest().getHeaders().get("X-User-Id"), getServiceName(path), path, ip);

        String key = userId + ":" + getServiceName(path);
        return userPlanService.getUserRateLimit(userId).flatMap( res -> {   
            System.out.println(res);
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
                    
                    // Business Message: Encourage subscription upgrade
                    String upgradeMessage = res <= 10 ? 
                        serviceConfig.getRateLimiting().getMessages().getUpgradeMessageBasic() :
                        serviceConfig.getRateLimiting().getMessages().getUpgradeMessagePremium();
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Message", upgradeMessage);
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Seconds", 
                        String.valueOf(serviceConfig.getRateLimiting().getMessages().getRetryAfterSeconds()));
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
        int limit;
        switch (service) {
            case "auth":
                limit = serviceConfig.getRateLimiting().getServiceLimits().getAuthRequestsPerMinute();
                break;
            case "movies":
                limit = serviceConfig.getRateLimiting().getServiceLimits().getMovieRequestsPerMinute();
                break;
            default:
                limit = serviceConfig.getRateLimiting().getServiceLimits().getDefaultRequestsPerMinute();
        }
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
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
