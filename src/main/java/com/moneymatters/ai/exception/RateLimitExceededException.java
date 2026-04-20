package com.moneymatters.ai.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {
    private final int limit;
    private final long retryAfterSeconds;

    public RateLimitExceededException(int limit, long retryAfterSeconds) {
        super("AI rate limit exceeded: " + limit + " requests per hour. Try again in "
            + retryAfterSeconds + " seconds.");
        this.limit = limit;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
