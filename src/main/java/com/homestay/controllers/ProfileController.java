package com.homestay.controllers;

import com.homestay.models.User;
import com.homestay.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/user/profile")
    public String viewProfile(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login?error=notfound";
        }
        model.addAttribute("user", user);
        model.addAttribute("avatarUrl", user.getAvatar());
        return "user/profile";
    }

    @GetMapping("/user/update-profile")
    public String updateProfilePage(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login?error=notfound";
        }
        model.addAttribute("user", user);
        return "user/update-profile";
    }

    @PostMapping("/user/update-profile")
    public String updateProfile(@RequestParam("username") String username,
                                @RequestParam("email") String email,
                                @RequestParam("phone") String phone,
                                @RequestParam(value = "avatar", required = false) MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login?error=notfound";
        }

        user.setEmail(email);
        user.setPhone(phone);

        if (!username.equals(user.getUsername())) {
            user.setUsername(username);
        }

        if (file != null && !file.isEmpty()) {
            try {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Tệp không hợp lệ!");
                    return "redirect:/user/update-profile";
                }
                String avatarPath = userService.saveImage(file);
                user.setAvatar(avatarPath);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload ảnh: " + e.getMessage());
                return "redirect:/user/update-profile";
            }
        }

        userService.save(user);
        updateAuthentication(user.getUsername()); // Cập nhật phiên đăng nhập

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/user/profile";
    }

    @PostMapping("/user/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login?error=notfound";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không chính xác!");
            return "redirect:/user/update-profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/update-profile";
        }

        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 8 ký tự!");
            return "redirect:/user/update-profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
        updateAuthentication(user.getUsername()); // Cập nhật phiên đăng nhập

        redirectAttributes.addFlashAttribute("successMessage", "Thay đổi mật khẩu thành công!");
        return "redirect:/user/update-profile";
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUsername(auth.getName()).orElse(null);
    }

    private void updateAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}
