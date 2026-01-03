package com.travelplanner.backend.controller;

import com.travelplanner.backend.model.Destination;
import com.travelplanner.backend.repository.DestinationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/destinations")
public class DestinationController {

    private final DestinationRepository repo;

    public DestinationController(DestinationRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Destination> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Destination create(@RequestBody Destination d) {
        return repo.save(d);
    }
}
