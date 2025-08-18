package com.hollywood.sweetspot.geo;

import com.hollywood.sweetspot.geo.dto.GeoAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/geo")
public class GeoController {

    private final GeoService geo;

    // 좌표 -> 주소
    @GetMapping("/reverse")
    public ResponseEntity<GeoAddress> reverse(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "ko") String lang
    ) {
        return ResponseEntity.ok(geo.reverse(lat, lng, lang));
    }

    // 주소 -> 좌표 (여러 후보 반환)
    @GetMapping("/geocode")
    public ResponseEntity<List<GeoAddress>> geocode(
            @RequestParam(name = "q") String query,
            @RequestParam(defaultValue = "ko") String lang
    ) {
        return ResponseEntity.ok(geo.geocode(query, lang));
    }
}