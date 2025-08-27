package com.hollywood.sweetspot.places;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
class PlaceController {
    private final PlaceSearchService svc;
    public PlaceController(PlaceSearchService svc) { this.svc = svc; }

    @GetMapping("/search")
    public List<PlaceDto> search(@RequestParam double lat,
                                 @RequestParam double lng,
                                 @RequestParam(defaultValue = "700") int radius,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false) String q,
                                 @RequestParam(defaultValue = "distance") String sort) {
        return svc.search(lat, lng, radius, category, q, sort);
    }
}