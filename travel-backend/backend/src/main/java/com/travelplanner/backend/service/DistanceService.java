package com.travelplanner.backend.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DistanceService {

    private final String API_KEY = "";

    public double getTravelTime(double startLat, double startLon,
                                double endLat, double endLon) {

        try {
            String url = "";


            RestTemplate rest = new RestTemplate();

            // JSON body
            JSONObject body = new JSONObject();
            JSONArray locations = new JSONArray();

            // ORS expects [lon, lat]
            locations.put(new JSONArray().put(startLon).put(startLat));
            locations.put(new JSONArray().put(endLon).put(endLat));

            body.put("locations", locations);
            body.put("metrics", new JSONArray().put("duration"));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", API_KEY);

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response =
                    rest.exchange(url, HttpMethod.POST, entity, String.class);

            JSONObject json = new JSONObject(response.getBody());

            double seconds = json
                    .getJSONArray("durations")
                    .getJSONArray(0)
                    .getDouble(1);

            return seconds / 60.0;  // convert to minutes

        } catch (Exception e) {
            System.out.println("DistanceService ERROR: " + e.getMessage());
            return -1; // invalid
        }
    }
}
