package com.homestay.controllers;

import com.homestay.models.Homestay;
import com.homestay.services.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homestays")
public class HomestayController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping
    public ResponseEntity<List<Homestay>> getAllHomestays() {
        List<Homestay> homestays = homestayService.getAllHomestays();
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Homestay> getHomestayById(@PathVariable Long id) {
        return homestayService.getHomestayById(id)
                .map(homestay -> new ResponseEntity<>(homestay, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Đảm bảo phương thức tìm kiếm đúng
    @GetMapping("/search")
    public ResponseEntity<List<Homestay>> getHomestaysByLocation(@RequestParam String location) {
        List<Homestay> homestays = homestayService.getHomestaysByLocation(location);
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Homestay> createHomestay(@RequestBody Homestay homestay) {
        if (homestay.getName() == null || homestay.getLocation() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(homestayService.createHomestay(homestay), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Homestay> updateHomestay(@PathVariable Long id, @RequestBody Homestay updatedHomestay) {
        return homestayService.updateHomestay(id, updatedHomestay)
                .map(homestay -> new ResponseEntity<>(homestay, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomestay(@PathVariable Long id) {
        return homestayService.deleteHomestay(id) ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}