package com.homestay.controllers;

import com.homestay.services.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class GuestController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping("/")
    public String guestPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("message", "Chào mừng bạn đến Homestay!");
        } else {
            model.addAttribute("message", "Chào mừng bạn quay lại, " + authentication.getName());
        }
        model.addAttribute("homestays", homestayService.getAllHomestays()); 
        return "guest/guest_dashboard";
    }
}
