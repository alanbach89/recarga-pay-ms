package org.example.service;

import java.time.LocalDateTime;

public interface RedisCacheService {
    void cacheSearchResult(String wallet, LocalDateTime date, Double result);

    Double getSearchResultFromCache(String wallet, LocalDateTime date);

    void clearCache(String wallet, LocalDateTime date);

    void clearCache();
}
