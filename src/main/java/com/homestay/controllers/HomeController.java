package com.homestay.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.homestay.services.HomestayService;
import com.homestay.services.UserService;
import com.homestay.models.User;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class HomeController {
    
    @Autowired
    private HomestayService homestayService;
    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String homePage(Model model) {
        // Lấy thông tin người dùng đã đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Lấy thông tin người dùng từ database
        Optional<User> user = userService.getUsername(username);
        
        if (user.isPresent()) {
            model.addAttribute("authenticatedUser", user.get());  // Thêm đối tượng người dùng vào model
        }

        String avatarUrl = "/uploads/avatars/defaultAvatar.jpg"; // Avatar mặc định nếu không có avatar
        if (user.isPresent() && user.get().getAvatar() != null) {
            avatarUrl = user.get().getAvatar();  // Cập nhật đường dẫn avatar từ cơ sở dữ liệu
        }

        model.addAttribute("avatarUrl", avatarUrl); // Thêm avatar vào model
        model.addAttribute("message", "Chào mừng bạn đến Homestay!");
        model.addAttribute("homestays", homestayService.getAllHomestays()); 

        return "user/home";  
    }
}
