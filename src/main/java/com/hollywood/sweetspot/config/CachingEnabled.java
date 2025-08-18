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
class CachingEnabled { }

@Configuration
class CacheConfigAddons {
    @Bean
    public CacheManager cacheManager() {
        var placeSearch = new CaffeineCache(
                "placeSearch",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(2))
                        .maximumSize(5_000)
                        .build());

        var geoReverse = new CaffeineCache(
                "geoReverse",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(10_000)
                        .build());

        var geoGeocode = new CaffeineCache(
                "geoGeocode",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(10_000)
                        .build());

        var mgr = new org.springframework.cache.support.SimpleCacheManager();
        mgr.setCaches(List.of(placeSearch, geoReverse, geoGeocode));
        return mgr;
    }
}