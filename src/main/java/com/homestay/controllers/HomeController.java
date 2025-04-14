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
            model.addAttribute("avatarUrl", user.getAvatar() != null ? user.getAvatar() : "images/default.jpg");
        });

        if (!Boolean.TRUE.equals(session.getAttribute("isFirstLogin"))) {
            session.setAttribute("isFirstLogin", true);
            model.addAttribute("firstLogin", true);
        } else {
            model.addAttribute("firstLogin", false);
        }

        // Danh sách các tỉnh/thành phố
        List<String> provinces = Arrays.asList(
            "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
            "Bình Thuận", "Nha Trang", "Đà Lạt", "Hạ Long", "Phú Quốc"
        );
        
        model.addAttribute("provinces", provinces);
        model.addAttribute("homestays", homestayService.getAllHomestays());

        return "user/home";
    }

    @GetMapping("/")
    public String home(Model model) {
        // Danh sách các tỉnh/thành phố phổ biến
        List<String> popularProvinces = Arrays.asList(
            "Bình Thuận", "Đà Lạt", "Nha Trang", "Phú Quốc", "Hội An",
            "Sapa", "Hạ Long", "Huế", "Đà Nẵng", "Hồ Chí Minh"
        );
        
        model.addAttribute("popularProvinces", popularProvinces);
        return "home";
    }

    @GetMapping("/province/{province}")
    public String showHomestaysByProvince(@PathVariable String province, Model model) {
        // Danh sách các tỉnh/thành phố
        List<String> provinces = Arrays.asList(
            "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
            "Bình Thuận", "Nha Trang", "Đà Lạt", "Hạ Long", "Phú Quốc"
        );
        
        List<Homestay> homestays = homestayService.getHomestaysByProvince(province);
        model.addAttribute("homestays", homestays);
        model.addAttribute("currentProvince", province);
        model.addAttribute("provinces", provinces);
        return "user/home";
    }
}
