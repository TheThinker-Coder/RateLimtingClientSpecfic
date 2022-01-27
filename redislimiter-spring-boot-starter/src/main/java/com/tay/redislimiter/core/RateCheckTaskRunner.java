package com.tay.redislimiter.core;

import com.tay.redislimiter.RedisLimiterProperties;
import com.tay.redislimiter.core.RedisRateLimiter;
import com.tay.redislimiter.core.RedisRateLimiterFactory;
import com.tay.redislimiter.event.RateCheckFailureEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.*;

@RequiredArgsConstructor
public final class RateCheckTaskRunner implements ApplicationContextAware {
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final RedisRateLimiterFactory redisRateLimiterFactory;

    private final RedisLimiterProperties redisLimiterProperties;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public boolean checkRun(String rateLimiterKey, TimeUnit timeUnit, int permits) {
        CheckTask task = new CheckTask(rateLimiterKey, timeUnit, permits);
        Future<Boolean> checkResult = executorService.submit(task);
        boolean retVal = true;
        try {
            retVal = checkResult.get(redisLimiterProperties.getCheckActionTimeout(), TimeUnit.MILLISECONDS);
        }
        catch(Exception e) {
            applicationContext.publishEvent(new RateCheckFailureEvent(e, "Access rate check task executed failed."));
        }
        return retVal;
    }

    class CheckTask implements Callable<Boolean> {
        private String rateLimiterKey;
        private TimeUnit timeUnit;
        private int permits;
        CheckTask(String rateLimiterKey, TimeUnit timeUnit, int permits) {
            this.rateLimiterKey = rateLimiterKey;
            this.timeUnit = timeUnit;
            this.permits = permits;
        }
        public Boolean call() {
            RedisRateLimiter redisRatelimiter = redisRateLimiterFactory.get(timeUnit);
            return redisRatelimiter.acquire(rateLimiterKey, permits);
        }
    }
}
