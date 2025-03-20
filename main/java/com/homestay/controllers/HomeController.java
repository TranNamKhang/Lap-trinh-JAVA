package com.homestay.controllers;

import com.homestay.models.Homestay;
import com.homestay.services.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private HomestayService homestayService;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        List<Homestay> homestays = homestayService.getAllHomestays();
        model.addAttribute("homestays", homestays);

        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        return "user/home";
    }
}
