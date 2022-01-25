
package com.tay.redislimiter;

import com.tay.redislimiter.core.RateCheckTaskRunner;
import com.tay.redislimiter.core.RedisRateLimiterFactory;
import com.tay.redislimiter.dynamic.LimiterConfigResource;
import com.tay.redislimiter.dynamic.RedisLimiterConfigProcessor;
import com.tay.redislimiter.event.DefaultRateCheckFailureListener;
import com.tay.redislimiter.event.DefaultRateExceedingListener;
import com.tay.redislimiter.event.RateCheckFailureListener;
import com.tay.redislimiter.event.RateExceedingListener;
import com.tay.redislimiter.web.RateCheckInterceptor;
import com.tay.redislimiter.web.RateLimiterWebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
@EnableConfigurationProperties(RedisLimiterProperties.class)
public class RedisLimiterConfiguration {

    @Autowired
    private RedisLimiterProperties redisLimiterProperties;

    @Bean
    @ConditionalOnMissingBean(JedisPool.class)
    public JedisPool jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisLimiterProperties.getRedisPoolMaxIdle());
        jedisPoolConfig.setMinIdle(redisLimiterProperties.getRedisPoolMinIdle());
        jedisPoolConfig.setMaxWaitMillis(redisLimiterProperties.getRedisPoolMaxWaitMillis());
        jedisPoolConfig.setMaxTotal(redisLimiterProperties.getRedisPoolMaxTotal());
        jedisPoolConfig.setTestOnBorrow(true);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisLimiterProperties.getRedisHost(), redisLimiterProperties.getRedisPort(), redisLimiterProperties.getRedisConnectionTimeout(), redisLimiterProperties.getRedisPassword());
        return jedisPool;
    }

    @Bean
    @ConditionalOnMissingBean(RedisRateLimiterFactory.class)
    public RedisRateLimiterFactory redisRateLimiterFactory() {
        RedisRateLimiterFactory redisRateLimiterFactory = new RedisRateLimiterFactory(jedisPool());
        return redisRateLimiterFactory;
    }

    @Bean
    @ConditionalOnMissingBean(RateCheckInterceptor.class)
    public RateCheckInterceptor rateCheckInterceptor() {
        RateCheckInterceptor rateCheckInterceptor;
        if (redisLimiterProperties.isEnableDynamicalConf()) {
            rateCheckInterceptor = new RateCheckInterceptor(redisLimiterProperties, rateCheckTaskRunner(), redisLimiterConfigProcessor());
        } else {
            rateCheckInterceptor = new RateCheckInterceptor(redisLimiterProperties, rateCheckTaskRunner(),null);
        }
        return rateCheckInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterWebMvcConfigurer.class)
    public RateLimiterWebMvcConfigurer rateLimiterWebMvcConfigurer() {
        RateLimiterWebMvcConfigurer rateLimiterWebMvcConfigurer = new RateLimiterWebMvcConfigurer(rateCheckInterceptor());
        return rateLimiterWebMvcConfigurer;
    }

    @Bean
    @ConditionalOnMissingBean(RateCheckTaskRunner.class)
    public RateCheckTaskRunner rateCheckTaskRunner() {
        RateCheckTaskRunner rateCheckTaskRunner = new RateCheckTaskRunner(redisRateLimiterFactory(), redisLimiterProperties);
        return rateCheckTaskRunner;
    }

    @Bean
    @ConditionalOnMissingBean(RateCheckFailureListener.class)
    public RateCheckFailureListener rateCheckFailureListener() {
        RateCheckFailureListener rateCheckFailureListener = new DefaultRateCheckFailureListener();
        return rateCheckFailureListener;
    }

    @Bean
    @ConditionalOnMissingBean(RateExceedingListener.class)
    public RateExceedingListener rateExceedingListener() {
        RateExceedingListener rateExceedingListener = new DefaultRateExceedingListener();
        return rateExceedingListener;
    }

    @Bean
    @ConditionalOnMissingBean(RedisLimiterConfigProcessor.class)
    @ConditionalOnProperty(prefix = "spring.redis-limiter", name = "enable-dynamical-conf", havingValue = "true")
    public RedisLimiterConfigProcessor redisLimiterConfigProcessor() {
        RedisLimiterConfigProcessor redisLimiterConfigProcessor = new RedisLimiterConfigProcessor(redisLimiterProperties);
        return redisLimiterConfigProcessor;
    }

    @Bean
    @ConditionalOnMissingBean(LimiterConfigResource.class)
    @ConditionalOnProperty(prefix = "spring.redis-limiter", name = "enable-dynamical-conf", havingValue = "true")
    public LimiterConfigResource limiterConfigResource() {
        LimiterConfigResource limiterConfigResource = new LimiterConfigResource(jedisPool(), redisLimiterProperties, redisLimiterConfigProcessor());
        return limiterConfigResource;
    }


}
