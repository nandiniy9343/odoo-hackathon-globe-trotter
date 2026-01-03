package com.travelplanner.backend.service;


import com.travelplanner.backend.model.Destination;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SafetyService {

    //  Weather risk mapping
    private static final Map<String, Double> WEATHER_RISK = Map.of(
            "clear", 0.0,
            "clouds", 0.1,
            "haze", 0.2,
            "rain", 0.6,
            "storm", 0.9
    );

    //  Tag-based place risk
    private static final Map<String, Double> TAG_RISK = Map.of(
            "waterfall", 0.6,
            "adventure", 0.5,
            "nightlife", 0.4,
            "beach", 0.3,
            "nature", 0.2,
            "shopping", 0.1,
            "temple", 0.05
    );

    public SafetyResult evaluate(
            List<Destination> places,
            String weather,
            double temperature
    ) {
        double score = 100;
        List<String> reasons = new ArrayList<>();

        double weatherRisk = WEATHER_RISK.getOrDefault(weather, 0.2);
        score -= weatherRisk * 30;
        reasons.add("Weather condition: " + weather);

        double tempRisk =
                temperature > 38 ? 0.8 :
                        temperature < 8  ? 0.6 : 0.1;

        score -= tempRisk * 20;
        reasons.add("Temperature: " + temperature + "°C");

        double placeRisk = places.stream()
                .mapToDouble(p -> calculateTagRisk(p.getTags()))
                .average()
                .orElse(0.1);

        score -= placeRisk * 25;
        reasons.add("Destination categories evaluated for safety");

        score = Math.max(0, Math.min(100, score));

        return new SafetyResult(
                (int) score,
                safetyLabel(score),
                reasons
        );
    }

    private double calculateTagRisk(String tags) {
        if (tags == null) return 0.1;

        return Arrays.stream(tags.split(","))
                .map(t -> TAG_RISK.getOrDefault(t.trim(), 0.1))
                .max(Double::compareTo)
                .orElse(0.1);
    }

    private String safetyLabel(double score) {
        if (score >= 80) return "Very Safe";
        if (score >= 60) return "Moderately Safe";
        if (score >= 40) return "Risky";
        return "Unsafe";
    }

    public record SafetyResult(
            int score,
            String label,
            List<String> reasons
    ) {}

}
