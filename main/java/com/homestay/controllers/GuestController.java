package com.homestay.controllers;

import com.homestay.services.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class GuestController {
    @Autowired
    private HomestayService homestayService;

    @GetMapping("/")
    public String Guest(Model model) {
        model.addAttribute("message", "Chào mừng bạn đến Homestay!");
        model.addAttribute("homestays", homestayService.getAllHomestays()); // Hiển thị tất cả homestay
        return "user/guest";
    }
}
