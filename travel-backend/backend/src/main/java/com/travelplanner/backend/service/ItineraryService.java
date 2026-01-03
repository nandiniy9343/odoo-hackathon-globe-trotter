package com.travelplanner.backend.service;

import com.travelplanner.backend.dto.ItineraryRequest;
import com.travelplanner.backend.dto.ItineraryResponse;
import com.travelplanner.backend.model.Destination;
import com.travelplanner.backend.repository.DestinationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItineraryService {

    private final DestinationRepository destinationRepository;
    private final WeatherService weatherService;
    private final DistanceService distanceService;
    private final SafetyService safetyService;

    public ItineraryService(DestinationRepository destinationRepository,
                            WeatherService weatherService,
                            DistanceService distanceService,
                            SafetyService safetyService
                            ) {

        this.destinationRepository = destinationRepository;
        this.weatherService = weatherService;
        this.distanceService = distanceService;
        this.safetyService = safetyService;


    }


    public ItineraryResponse generate(ItineraryRequest request) {

        //  1. Get Weather
        WeatherService.WeatherInfo weatherInfo = weatherService.getWeather(request.getCity());
        String weather = weatherInfo.getCondition();  // clear, rain, haze, etc
        double temp = weatherInfo.getTemperature();   // °C

        //  2. Fetch all destinations from DB
        List<Destination> allPlaces = destinationRepository.findAll();

        //  3. Filter by city
        List<Destination> cityPlaces = allPlaces.stream()
                .filter(d -> d.getCity().equalsIgnoreCase(request.getCity()))
                .toList();

        //  4. Filter by moods + score
        List<Destination> moodFiltered = cityPlaces.stream()
                .filter(d -> matchesMood(d.getTags(), request.getMoods()))
                .sorted((a, b) -> Double.compare(
                        calculateScore(b, request.getMoods()),
                        calculateScore(a, request.getMoods())
                ))
                .toList();

        //  5. WEATHER FILTERING
        if (weather.contains("rain") || weather.contains("storm")) {
            moodFiltered = moodFiltered.stream()
                    .filter(d -> !d.getTags().contains("waterfall"))
                    .filter(d -> !d.getTags().contains("beach"))
                    .filter(d -> !d.getTags().contains("nature"))
                    .filter(d -> !d.getTags().contains("adventure"))
                    .toList();
        }

        if (weather.contains("haze") || weather.contains("fog")) {
            moodFiltered = moodFiltered.stream()
                    .filter(d -> !d.getTags().contains("scenic"))
                    .filter(d -> !d.getTags().contains("photography"))
                    .toList();
        }

        if (temp > 35) {
            moodFiltered = moodFiltered.stream()
                    .filter(d -> !d.getTags().contains("outdoor"))
                    .filter(d -> !d.getTags().contains("adventure"))
                    .filter(d -> !d.getTags().contains("nature"))
                    .toList();
        }

        if (temp < 10) {
            moodFiltered = moodFiltered.stream()
                    .filter(d -> !d.getTags().contains("outdoor"))
                    .filter(d -> !d.getTags().contains("nature"))
                    .toList();
        }

        //  6. Fallback if not enough results
        if (moodFiltered.size() < request.getDays() * 3) {
            List<Destination> copy = new ArrayList<>(moodFiltered);

            List<Destination> popularPlaces = cityPlaces.stream()
                    .filter(d -> !copy.contains(d))
                    .sorted((a, b) -> Double.compare(
                            b.getPopularity(),
                            a.getPopularity()
                    ))
                    .toList();

            List<Destination> combined = new ArrayList<>(moodFiltered);
            combined.addAll(popularPlaces);
            moodFiltered = combined;
        }

        // 7. SAFETY EVALUATION
        SafetyService.SafetyResult safety =
                safetyService.evaluate(cityPlaces, weather, temp);


        //  8. Build itinerary days
        List<ItineraryResponse.ItineraryDay> days =
                buildDays(moodFiltered, request.getDays(), request.getBudget());

        //  9. Build final response
        ItineraryResponse response = new ItineraryResponse();
        response.setDays(days);
        response.setTotalDays(request.getDays());
        response.setTotalBudget(request.getBudget());
        response.setWeather(weather);
        response.setTemperature(temp);
        response.setSafetyScore(safety.score());
        response.setSafetyLabel(safety.label());
        response.setSafetyReasons(safety.reasons());

        return response;
    }


    private boolean matchesMood(String tags, List<String> moods) {
        if (tags == null) return false;
        for (String mood : moods) {
            if (tags.toLowerCase().contains(mood.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private List<ItineraryResponse.ItineraryDay> buildDays(
            List<Destination> places,
            int totalDays,
            int totalBudget) {

        List<ItineraryResponse.ItineraryDay> days = new ArrayList<>();

        int perDayBudget = totalBudget / totalDays;
        List<Destination> remaining = new ArrayList<>(places); // copy list

        for (int day = 1; day <= totalDays; day++) {

            ItineraryResponse.ItineraryDay dayPlan = new ItineraryResponse.ItineraryDay();
            dayPlan.setDayNumber(day);

            List<ItineraryResponse.ItineraryPlace> selected = new ArrayList<>();
            double hours = 0;
            int spent = 0;

            Destination current = null; // starting place

            // max 3 places per day
            for (int count = 0; count < 3 && !remaining.isEmpty(); ) {

                Destination next;

                // FIRST PLACE → pick top place from remaining
                if (current == null) {
                    next = remaining.get(0);
                } else {
                    // find nearest place based on travel time
                    next = findNearestPlace(current, remaining);
                }

                if (next == null) break;


                double travelTime = 0.0;

                // For next places
                if (current != null) {

                    travelTime = distanceService.getTravelTime(
                            current.getLat(), current.getLon(),
                            next.getLat(), next.getLon()
                    );

                    // safeguard: if API fails or returns -1
                    if (travelTime < 0) travelTime = 0.0;

                    // skip far places
                    if (travelTime > 45) {
                        remaining.remove(next);
                        continue;
                    }

                    // skip if day hours exceed limit
                    if (hours + (travelTime / 60.0) > 8) {
                        remaining.remove(next);
                        continue;
                    }
                }

                // budget check
                if (spent + next.getAvgCost() > perDayBudget) {
                    remaining.remove(next);
                    continue;
                }

                // add travel time to day time
                hours += travelTime / 60.0;

                //  create itinerary place
                ItineraryResponse.ItineraryPlace p = new ItineraryResponse.ItineraryPlace();
                p.setId(next.getId());
                p.setName(next.getName());
                p.setDuration(next.getDurationHrs());
                p.setCost(next.getAvgCost());
                p.setTravelTime(travelTime); // NEVER null now
                p.setMapUrl("https://www.google.com/maps/search/?api=1&query="
                        + next.getLat() + "," + next.getLon());

                selected.add(p);

                // update counters
                spent += next.getAvgCost();
                hours += next.getDurationHrs();
                count++;

                // move to next
                current = next;
                remaining.remove(next);
            }

            dayPlan.setPlaces(selected);
            days.add(dayPlan);
        }

        return days;
    }


    private Destination findNearestPlace(Destination from, List<Destination> list) {

        Destination best = null;
        double bestTime = Double.MAX_VALUE;

        for (Destination d : list) {
            double travel = distanceService.getTravelTime(
                    from.getLat(), from.getLon(),
                    d.getLat(), d.getLon()
            );

            if (travel >= 0 && travel < bestTime) {
                bestTime = travel;
                best = d;
            }
        }
        return best;
    }



    private double calculateScore(Destination d, List<String> moods) {
        double score = 0;

        for (String mood : moods) {
            if (d.getTags().toLowerCase().contains(mood.toLowerCase())) {
                score += 50;
            }
        }

        if (d.getRating() != null) {
            score += d.getRating() * 10;
        }

        if (d.getPopularity() != null) {
            score += d.getPopularity() * 0.2;
        }

        if (d.getAvgCost() != null) {
            score += Math.max(0, (200 - d.getAvgCost()));
        }

        return score;
    }


}
