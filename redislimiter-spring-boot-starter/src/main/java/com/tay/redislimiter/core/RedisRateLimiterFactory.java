
package com.tay.redislimiter.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class RedisRateLimiterFactory {

    private final JedisPool jedisPool;

    private Cache<TimeUnit, RedisRateLimiter> redisRateLimiterCache =
            Caffeine.newBuilder().maximumSize(10).build();

    public RedisRateLimiter get(TimeUnit timeUnit) {
        RedisRateLimiter redisRateLimiter = redisRateLimiterCache.getIfPresent(timeUnit);
        if(redisRateLimiter == null) {
            synchronized (RedisRateLimiterFactory.class) {
                redisRateLimiter = redisRateLimiterCache.getIfPresent(timeUnit);
                if(redisRateLimiter == null) {
                    redisRateLimiter = new RedisRateLimiter(jedisPool, timeUnit);
                    redisRateLimiterCache.put(timeUnit, redisRateLimiter);
                }
            }
        }
        return redisRateLimiter;
    }
}
