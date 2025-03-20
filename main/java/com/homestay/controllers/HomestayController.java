package com.homestay.controllers;

import com.homestay.models.Homestay;
import com.homestay.services.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/homestays")
public class HomestayController {
    @Autowired
    private HomestayService homestayService;

    @GetMapping("/")
    public List<Homestay> getAllHomestays() {
        return homestayService.getAllHomestays();
    }

    @GetMapping("/{id}")
    public Optional<Homestay> getHomestayById(@PathVariable Long id) {
        return homestayService.getHomestayById(id);
    }

    @GetMapping("/search")
    public List<Homestay> getHomestaysByLocation(@RequestParam String location) {
        return homestayService.getHomestaysByLocation(location);
    }

    @PostMapping("/")
    public Homestay createHomestay(@RequestBody Homestay homestay) {
        return homestayService.createHomestay(homestay);
    }
}
