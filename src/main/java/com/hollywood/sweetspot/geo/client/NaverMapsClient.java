package com.hollywood.sweetspot.geo.client;

import com.hollywood.sweetspot.geo.dto.GeoAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NaverMapsClient {

    private final WebClient naverMapsWebClient;

    /**
     * 주소 → 좌표 (최대 N개 반환)
     */
    public List<GeoAddress> geocode(String query, String lang) {
        try {
            var url = "/map-geocode/v2/geocode?query=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    (lang != null && lang.equalsIgnoreCase("en") ? "&language=eng" : "");
            JsonNode root = naverMapsWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<GeoAddress> list = new ArrayList<>();
            if (root == null || !root.has("addresses")) return list;

            for (JsonNode a : root.get("addresses")) {
                GeoAddress dto = GeoAddress.builder()
                        .roadAddress(a.path("roadAddress").asText(null))
                        .jibunAddress(a.path("jibunAddress").asText(null))
                        .englishAddress(a.path("englishAddress").asText(null))
                        .sido(extractAddrElement(a, "SIDO"))
                        .sigungu(extractAddrElement(a, "SIGUGUN"))
                        .dongmyun(extractAddrElement(a, "DONGMYUN"))
                        .ri(extractAddrElement(a, "RI"))
                        .postalCode(extractAddrElement(a, "POSTAL_CODE"))
                        .lng(parseDouble(a.path("x").asText(null)))
                        .lat(parseDouble(a.path("y").asText(null)))
                        .provider("NAVER")
                        .build();
                list.add(dto);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("NAVER geocode failed: " + e.getMessage(), e);
        }
    }

    /**
     * 좌표 → 주소 (1개 대표 결과)
     * Naver Reverse는 coords=lng,lat 주의!
     */
    public Optional<GeoAddress> reverse(double lat, double lng) {
        try {
            String coords = lng + "," + lat; // NAVER는 (x,y)=(lng,lat)
            String url = "/map-reversegeocode/v2/gc?coords=" + coords +
                    "&output=json&orders=roadaddr,addr,admcode,legalcode";
            JsonNode root = naverMapsWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root == null || !root.has("results")) return Optional.empty();

            // roadaddr 우선, 없으면 addr → admcode 순
            JsonNode road = findResult(root.get("results"), "roadaddr");
            JsonNode addr = (road != null) ? road : findResult(root.get("results"), "addr");
            JsonNode adm  = findResult(root.get("results"), "admcode");
            JsonNode legal= findResult(root.get("results"), "legalcode");

            String sido = regionName(adm, legal, "area1");
            String sigungu = regionName(adm, legal, "area2");
            String dong = regionName(adm, legal, "area3");
            String ri = regionName(adm, legal, "area4");

            String roadAddress = (road != null) ? composeRoadAddress(road, sido, sigungu, dong) : null;
            String jibunAddress = (addr != null) ? composeJibunAddress(addr, sido, sigungu, dong, ri) : null;

            GeoAddress dto = GeoAddress.builder()
                    .roadAddress(emptyToNull(roadAddress))
                    .jibunAddress(emptyToNull(jibunAddress))
                    .englishAddress(null) // reverse 응답엔 보통 영문 주소 없음
                    .sido(sido)
                    .sigungu(sigungu)
                    .dongmyun(dong)
                    .ri(ri)
                    .postalCode(null)
                    .lat(lat)
                    .lng(lng)
                    .provider("NAVER")
                    .build();

            return Optional.of(dto);
        } catch (Exception e) {
            throw new RuntimeException("NAVER reverseGeocode failed: " + e.getMessage(), e);
        }
    }

    // ---------- helpers ----------

    private static JsonNode findResult(JsonNode results, String name) {
        if (results == null) return null;
        for (JsonNode r : results) {
            if (name.equalsIgnoreCase(r.path("name").asText())) return r;
        }
        return null;
    }

    private static String regionName(JsonNode adm, JsonNode legal, String areaKey) {
        String fromAdm = (adm == null) ? null : adm.path("region").path(areaKey).path("name").asText(null);
        String fromLegal = (legal == null) ? null : legal.path("region").path(areaKey).path("name").asText(null);
        // 우선순위: admcode → legalcode
        return firstNonEmpty(fromAdm, fromLegal);
    }

    private static String composeRoadAddress(JsonNode road, String sido, String sigungu, String dong) {
        // results[].land.name(도로명) + number1(-number2)
        JsonNode land = road.path("land");
        String rname = land.path("name").asText("");
        String n1 = land.path("number1").asText("");
        String n2 = land.path("number2").asText("");
        String nn = n1 + (n2 != null && !n2.isBlank() ? "-" + n2 : "");
        return join(" ", sido, sigungu, dong, rname, nn);
    }

    private static String composeJibunAddress(JsonNode addr, String sido, String sigungu, String dong, String ri) {
        // 지번: 시도 시군구 동(면) 리 + 본번-부번
        JsonNode land = addr.path("land");
        String n1 = land.path("number1").asText("");
        String n2 = land.path("number2").asText("");
        String nn = n1 + (n2 != null && !n2.isBlank() ? "-" + n2 : "");
        return join(" ", sido, sigungu, dong, ri, nn);
    }

    private static String extractAddrElement(JsonNode a, String type) {
        if (a == null || !a.has("addressElements")) return null;
        for (JsonNode el : a.get("addressElements")) {
            for (JsonNode t : el.path("types")) {
                if (type.equalsIgnoreCase(t.asText())) {
                    if ("POSTAL_CODE".equalsIgnoreCase(type)) {
                        return el.path("longName").asText(null);
                    }
                    return el.path("longName").asText(null);
                }
            }
        }
        return null;
    }

    private static Double parseDouble(String s) {
        try { return (s == null) ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }

    private static String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isBlank()) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(p);
        }
        return sb.toString();
    }

    private static String firstNonEmpty(String... s) {
        for (String v : s) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}