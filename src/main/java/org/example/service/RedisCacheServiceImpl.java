package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    private static final Logger logger = LogManager.getLogger(RedisCacheServiceImpl.class);

    private final String separator = "#";

    @Autowired
    private RedisTemplate<String, Double> redisTemplate;

    @Value("${redis.cache.ttl:3600}")
    private long ttl; // Default to 1 hour if not specified


    private String generateCacheKey(String wallet, String date) {
        return wallet + separator + date;
    }

    private String formatedDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    @Override
    public void cacheSearchResult(String wallet, LocalDateTime date, Double result) {
        try {
            String key = generateCacheKey(wallet, formatedDate(date));
            redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public Double getSearchResultFromCache(String wallet, LocalDateTime date) {
        try {
            String key = generateCacheKey(wallet, formatedDate(date));
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void clearCache(String wallet, LocalDateTime date) {
        try {
            String key = generateCacheKey(wallet, formatedDate(date));
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void clearCache() {
        try {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
