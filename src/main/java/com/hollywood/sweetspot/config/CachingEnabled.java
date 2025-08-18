package com.hollywood.sweetspot.config;

import java.time.Duration;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
class CachingEnabled {
}

@Configuration
class CacheConfigAddons {
    @Bean
    public CacheManager cacheManager() {
        var cache = new CaffeineCache(
                "placeSearch",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(2))
                        .maximumSize(5000)
                        .build());
        var mgr = new org.springframework.cache.support.SimpleCacheManager();
        mgr.setCaches(List.of(cache));
        return mgr;
    }
}