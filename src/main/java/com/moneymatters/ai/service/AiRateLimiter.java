package com.moneymatters.ai.service;

import com.moneymatters.ai.config.AiRateLimitProperties;
import com.moneymatters.ai.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AiRateLimiter {

    private static final Duration WINDOW = Duration.ofHours(1);
    private final AiRateLimitProperties props;
    private final Map<String, Deque<Instant>> userHits = new ConcurrentHashMap<>();

    public void checkAndConsume(String userId) {
        int limit = props.getMaxRequestsPerHour();
        Instant now = Instant.now();
        Instant cutoff = now.minus(WINDOW);

        Deque<Instant> hits = userHits.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst().isBefore(cutoff)) {
                hits.pollFirst();
            }
            if (hits.size() >= limit) {
                Instant oldest = hits.peekFirst();
                long retryAfter = Duration.between(now, oldest.plus(WINDOW)).getSeconds();
                throw new RateLimitExceededException(limit, Math.max(retryAfter, 1));
            }
            hits.addLast(now);
        }
    }

    public int remaining(String userId) {
        Deque<Instant> hits = userHits.get(userId);
        if (hits == null) return props.getMaxRequestsPerHour();
        Instant cutoff = Instant.now().minus(WINDOW);
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst().isBefore(cutoff)) {
                hits.pollFirst();
            }
            return Math.max(0, props.getMaxRequestsPerHour() - hits.size());
        }
    }
}
