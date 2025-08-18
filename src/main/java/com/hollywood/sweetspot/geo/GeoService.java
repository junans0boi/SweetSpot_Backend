package com.hollywood.sweetspot.geo;

import com.hollywood.sweetspot.geo.client.NaverMapsClient;
import com.hollywood.sweetspot.geo.dto.GeoAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final NaverMapsClient naver;

    @Cacheable(cacheNames = "geoReverse", key = "T(java.lang.String).format('%.6f,%.6f', #lat, #lng)")
    public GeoAddress reverse(double lat, double lng, String lang) {
        validateLatLng(lat, lng);
        return naver.reverse(lat, lng)
                .orElseThrow(() -> new RuntimeException("No reverse-geocode result"));
    }

    @Cacheable(cacheNames = "geoGeocode", key = "#query.trim().toLowerCase() + '|' + (#lang == null ? 'ko' : #lang)")
    public List<GeoAddress> geocode(String query, String lang) {
        if (query == null || query.isBlank())
            throw new IllegalArgumentException("query required");
        return naver.geocode(query.trim(), lang);
    }

    private void validateLatLng(double lat, double lng) {
        if (lat < -90 || lat > 90) throw new IllegalArgumentException("lat out of range");
        if (lng < -180 || lng > 180) throw new IllegalArgumentException("lng out of range");
    }
}