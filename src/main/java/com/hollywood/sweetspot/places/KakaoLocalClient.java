package com.hollywood.sweetspot.places;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoLocalClient {
    private final WebClient webClient;

    public List<KakaoModels.KakaoPlace> searchByCategory(String groupCode, double lat, double lng, int radius, String query) {
        return webClient.get()
                .uri(uri -> uri.path("/v2/local/search/category.json")
                        .queryParam("category_group_code", groupCode)
                        .queryParam("y", lat)
                        .queryParam("x", lng)
                        .queryParam("radius", Math.min(radius, 20000))
                        // 카테고리 API는 query를 무시할 수 있음. 유지하되 optional로.
                        .queryParamIfPresent("query",
                                (query == null || query.isBlank())
                                        ? java.util.Optional.empty()
                                        : java.util.Optional.of(query))
                        .build())
                .retrieve()
                .bodyToMono(KakaoModels.KakaoResp.class)
                .map(resp -> {
                    List<KakaoModels.KakaoPlace> docs = (resp == null) ? null : resp.documents();
                    return (docs != null) ? docs : Collections.<KakaoModels.KakaoPlace>emptyList();
                })
                .onErrorResume(e -> {
                    log.warn("Kakao category search failed: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .block();
    }

    public List<KakaoModels.KakaoPlace> searchByKeyword(String keyword, double lat, double lng, int radius) {
        return webClient.get()
                .uri(uri -> uri.path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .queryParam("y", lat)
                        .queryParam("x", lng)
                        .queryParam("radius", Math.min(radius, 20000))
                        .build())
                .retrieve()
                .bodyToMono(KakaoModels.KakaoResp.class)
                .map(resp -> {
                    List<KakaoModels.KakaoPlace> docs = (resp == null) ? null : resp.documents();
                    return (docs != null) ? docs : Collections.<KakaoModels.KakaoPlace>emptyList();
                })
                .onErrorResume(e -> {
                    log.warn("Kakao keyword search failed: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .block();
    }
}