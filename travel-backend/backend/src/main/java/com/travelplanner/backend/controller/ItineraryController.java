package com.travelplanner.backend.controller;

import com.travelplanner.backend.dto.ItineraryRequest;
import com.travelplanner.backend.dto.ItineraryResponse;
import com.travelplanner.backend.service.ItineraryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itinerary")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping("/generate")
    public ItineraryResponse generate(@RequestBody ItineraryRequest request) {
        return itineraryService.generate(request);
    }
}
