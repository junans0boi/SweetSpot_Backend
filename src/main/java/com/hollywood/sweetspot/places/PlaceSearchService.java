package com.hollywood.sweetspot.places;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceSearchService {
    private final KakaoLocalClient kakao;
    private final CategoryMapper mapper;

    public PlaceSearchService(KakaoLocalClient kakao, CategoryMapper mapper) {
        this.kakao = kakao;
        this.mapper = mapper;
    }

    @Cacheable(cacheNames = "placeSearch",
               key = "#lat+'_'+#lng+'_'+#radius+'_'+#category+'_'+#q+'_'+#sort")
    public List<PlaceDto> search(double lat,
                                 double lng,
                                 int radius,
                                 String category,
                                 String q,
                                 String sort) {
        var mk = mapper.map(category);

        List<KakaoModels.KakaoPlace> raw;
        if (mk != null) {
            // 카테고리 검색 + 보조 키워드(없으면 힌트 1개 자동 사용)
            String hint = (!mk.keywordHints().isEmpty() && (q == null || q.isBlank()))
                    ? mk.keywordHints().get(0)
                    : q;
            raw = kakao.searchByCategory(mk.groupCode(), lat, lng, radius, hint);
        } else if (q != null && !q.isBlank()) {
            // 키워드 검색
            raw = kakao.searchByKeyword(q, lat, lng, radius);
        } else {
            raw = List.of();
        }

        var list = raw.stream()
                .map(p -> toDto(p, lat, lng))
                .collect(Collectors.toList());

        // 기본: 거리순 정렬 (distanceMeters 가 null일 수 있으므로 nullsLast)
        if (sort == null || sort.isBlank() || "distance".equalsIgnoreCase(sort)) {
            list.sort(Comparator.comparing(
                    PlaceDto::distanceMeters,
                    Comparator.nullsLast(Double::compareTo)
            ));
        }
        // TODO: 정확도/추천 정렬은 추후 점수 모델 도입 시 구현
        return list;
    }

    private PlaceDto toDto(KakaoModels.KakaoPlace p, double baseLat, double baseLng) {
        Double plat = safeParse(p.y());
        Double plng = safeParse(p.x());
        Double dist = (plat != null && plng != null)
                ? Haversine.distanceMeters(baseLat, baseLng, plat, plng)
                : null;

        String address = (p.roadAddressName() != null && !p.roadAddressName().isBlank())
                ? p.roadAddressName()
                : p.addressName();

        return new PlaceDto(
                "kakao",
                p.id(),
                p.placeName(),
                address,
                plat,
                plng,
                dist
        );
    }

    private static Double safeParse(String s) {
        try { return (s == null) ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }
}