package com.hollywood.sweetspot.places;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class KakaoModels {
    public record KakaoPlace(
            String id,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address_name") String roadAddressName,
            String x, // lng
            String y  // lat
    ) {}
    public record KakaoResp(List<KakaoPlace> documents) {}
}