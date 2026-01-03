package com.travelplanner.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data

@Entity
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String city;
    private String tags;
    private Integer avgCost;
    private Double durationHrs;
    private Double rating;
    private Integer popularity;
    private Double lat;
    private Double lon;


    // Getters & Setters
}
