package com.homestay.controllers;

import com.homestay.services.CategoryService;
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
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String guestPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("message", "Chào mừng bạn đến Homestay!");
        } else {
            model.addAttribute("message", "Chào mừng bạn quay lại, " + authentication.getName());
        }
        model.addAttribute("homestays", homestayService.getAllHomestays()); // Hiển thị tất cả homestay
        return "guest/guest_dashboard";
    }
    @GetMapping("/categories")
    public String showCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "user/category";
    }
}
