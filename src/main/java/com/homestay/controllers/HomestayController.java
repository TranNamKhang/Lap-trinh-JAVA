package com.homestay.controllers;

import com.homestay.models.Homestay;
import com.homestay.services.HomestayService;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
@RequestMapping("/user/homestays")
public class HomestayController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Homestay>> getAllHomestays() {
        List<Homestay> homestays = homestayService.getAllHomestays();
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Homestay> getHomestayById(@PathVariable Long id) {
        return homestayService.getHomestayById(id)
                .map(homestay -> new ResponseEntity<>(homestay, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public String getHomestayDetail(@PathVariable Long id, Model model) {
        Optional<Homestay> homestay = homestayService.getHomestayById(id);
        if (homestay.isPresent()) {
            model.addAttribute("homestay", homestay.get());
            return "user/homestay-detail";
        }
        return "redirect:/user/home?error=notfound";
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Homestay>> getHomestaysByLocation(@RequestParam String location) {
        List<Homestay> homestays = homestayService.getHomestaysByLocation(location);
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @GetMapping("/api/search/province")
    @ResponseBody
    public ResponseEntity<List<Homestay>> getHomestaysByProvince(@RequestParam String province) {
        List<Homestay> homestays = homestayService.getHomestaysByProvince(province);
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Homestay> createHomestay(@RequestBody Homestay homestay) {
        if (homestay.getName() == null || homestay.getLocation() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(homestayService.createHomestay(homestay), HttpStatus.CREATED);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Homestay> updateHomestay(@PathVariable Long id, @RequestBody Homestay updatedHomestay) {
        return homestayService.updateHomestay(id, updatedHomestay)
                .map(homestay -> new ResponseEntity<>(homestay, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteHomestay(@PathVariable Long id) {
        return homestayService.deleteHomestay(id) ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
