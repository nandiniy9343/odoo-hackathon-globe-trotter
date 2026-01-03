package com.travelplanner.backend.controller;

import com.travelplanner.backend.service.WeatherService;
import com.travelplanner.backend.service.WeatherService.WeatherInfo;
import org.springframework.web.bind.annotation.*;

@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public WeatherInfo getWeather(@RequestParam String city) {
        return weatherService.getWeather(city);
    }
}
