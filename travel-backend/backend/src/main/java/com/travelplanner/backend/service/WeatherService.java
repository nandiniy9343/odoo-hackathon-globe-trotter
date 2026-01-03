package com.travelplanner.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class WeatherService {

    private final String API_KEY = "";

    public WeatherInfo getWeather(String city) {

        try {
            String url = ""
                    + city + "&appid=" + "" + "&units=metric";


            RestTemplate rest = new RestTemplate();
            String response = rest.getForObject(url, String.class);

            JSONObject json = new JSONObject(response);

            String condition = json
                    .getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("main")
                    .toLowerCase();

            double temp = json
                    .getJSONObject("main")
                    .getDouble("temp");

            return new WeatherInfo(condition, temp);

        } catch (Exception e) {
            return new WeatherInfo("unknown", 0.0);
        }
    }

    // Small helper class to store results
    public static class WeatherInfo {
        private String condition;
        private double temperature;

        public WeatherInfo(String condition, double temperature) {
            this.condition = condition;
            this.temperature = temperature;
        }

        public String getCondition() {
            return condition;
        }

        public double getTemperature() {
            return temperature;
        }
    }
}
