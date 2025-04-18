package com.homestay.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;
import com.homestay.services.HomestayService;
import com.homestay.services.UserService;
import com.homestay.models.User;
import java.util.Optional;
import com.homestay.models.Homestay;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/user")
public class HomeController {
    
    @Autowired
    private HomestayService homestayService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String homePage(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/auth/login";
        }

        String username = authentication.getName();
        Optional<User> optionalUser = userService.getUsername(username);

        optionalUser.ifPresent(user -> {
            model.addAttribute("authenticatedUser", user);
            model.addAttribute("avatarUrl", user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : "/images/default.jpg");
        });

        if (!Boolean.TRUE.equals(session.getAttribute("isFirstLogin"))) {
            session.setAttribute("isFirstLogin", true);
            model.addAttribute("firstLogin", true);
        } else {
            model.addAttribute("firstLogin", false);
        }

        List<String> provinces = Arrays.asList(
            "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
            "Bình Thuận", "Nha Trang", "Đà Lạt", "Hạ Long", "Phú Quốc"
        );
        
        List<Homestay> homestays = homestayService.getAllHomestays();
        Map<Long, Double> averageRatings = new HashMap<>();
        for (Homestay homestay : homestays) {
            averageRatings.put(homestay.getId(), homestayService.getAverageRating(homestay));
        }
        
        model.addAttribute("provinces", provinces);
        model.addAttribute("homestays", homestays);
        model.addAttribute("averageRatings", averageRatings);

        return "user/home";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/auth/login";
        }

        String username = authentication.getName();
        Optional<User> optionalUser = userService.getUsername(username);

        optionalUser.ifPresent(user -> {
            model.addAttribute("authenticatedUser", user);
            model.addAttribute("avatarUrl", user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : "/images/default.jpg");
        });

        return "user/about";
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/auth/login";
        }

        String username = authentication.getName();
        Optional<User> optionalUser = userService.getUsername(username);

        optionalUser.ifPresent(user -> {
            model.addAttribute("authenticatedUser", user);
            model.addAttribute("avatarUrl", user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : "/images/default.jpg");
        });

        return "user/contact";
    }

    @GetMapping("/")
    public String home(Model model) {
        List<String> popularProvinces = Arrays.asList(
            "Bình Thuận", "Đà Lạt", "Nha Trang", "Phú Quốc", "Hội An",
            "Sapa", "Hạ Long", "Huế", "Đà Nẵng", "Hồ Chí Minh"
        );
        
        model.addAttribute("popularProvinces", popularProvinces);
        return "home";
    }

    @GetMapping("/province/{province}")
    public String showHomestaysByProvince(@PathVariable String province, Model model) {
        List<String> provinces = Arrays.asList(
            "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
            "Bình Thuận", "Nha Trang", "Đà Lạt", "Hạ Long", "Phú Quốc"
        );
        
        List<Homestay> homestays = homestayService.getHomestaysByProvince(province);
        Map<Long, Double> averageRatings = new HashMap<>();
        for (Homestay homestay : homestays) {
            averageRatings.put(homestay.getId(), homestayService.getAverageRating(homestay));
        }
        
        model.addAttribute("homestays", homestays);
        model.addAttribute("currentProvince", province);
        model.addAttribute("provinces", provinces);
        model.addAttribute("averageRatings", averageRatings);
        return "user/home";
    }
}
