package com.travelplanner.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ItineraryResponse {

    private int totalDays;
    private int totalBudget;
    private String weather;
    private Double temperature;
    private int safetyScore;
    private String safetyLabel;
    private List<String> safetyReasons;
    private List<ItineraryDay> days;

    @Data
    public static class ItineraryDay {
        private int dayNumber;
        private List<ItineraryPlace> places;
    }

    @Data
    public static class ItineraryPlace {
        private Long id;
        private String name;
        private Integer cost;
        private Double duration;
        private Double travelTime;
        private String mapUrl;

    }


}
