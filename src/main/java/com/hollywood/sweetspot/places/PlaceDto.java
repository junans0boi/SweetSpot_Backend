package com.hollywood.sweetspot.places;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaceDto(
        String provider,
        String externalId,
        String name,
        String address,
        Double lat,
        Double lng,
        Double distanceMeters
) {}