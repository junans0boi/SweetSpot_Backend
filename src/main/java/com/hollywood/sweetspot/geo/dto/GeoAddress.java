package com.hollywood.sweetspot.geo.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeoAddress {
    // 표준화된 주소 필드
    private String roadAddress;     // 도로명 주소(가능하면)
    private String jibunAddress;    // 지번 주소(가능하면)
    private String englishAddress;  // 영문 주소(geocode에서만 보통 제공)
    private String sido;            // 시/도
    private String sigungu;         // 시/군/구
    private String dongmyun;        // 동/면
    private String ri;              // 리(있으면)
    private String postalCode;      // 우편번호(있으면)

    private double lat;             // 위도
    private double lng;             // 경도

    private String provider;        // "NAVER"
}