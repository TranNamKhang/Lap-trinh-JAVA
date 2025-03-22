package com.homestay.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.homestay.services.HomestayService;

@Controller
@RequestMapping("/user")
public class HomeController {
    @Autowired
    private HomestayService homestayService;

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("message", "Chào mừng bạn đến Homestay!");
        model.addAttribute("homestays", homestayService.getAllHomestays()); 
        return "user/home";  
    }

    @GetMapping("/services")
    public String showServices() {
        return "user/services"; 
    }
}
