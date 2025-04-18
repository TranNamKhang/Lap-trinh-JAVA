package com.homestay.controllers;

import com.homestay.models.User;
import com.homestay.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @ModelAttribute("message") String message,
                            Model model) {
        if (error != null) {
            model.addAttribute("message", "Sai tài khoản hoặc mật khẩu!");
        }
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginRedirect() {
        return "redirect:/user/dashboard";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @GetMapping("/dashboard")
    public String guestLogin(Model model) {
        model.addAttribute("message", "Chào mừng bạn đến Homestay!");
        return "user/dashboard";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("message", "Tên đăng nhập đã tồn tại!");
                return "redirect:/auth/register";
            }

            if (userService.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("message", "Email đã tồn tại!");
                return "redirect:/auth/register";
            }

            if (userService.existsByPhone(user.getPhone())) {
                redirectAttributes.addFlashAttribute("message", "Số điện thoại đã tồn tại!");
                return "redirect:/auth/register";
            }

            if (user.getPassword().length() < 8) {
                redirectAttributes.addFlashAttribute("message", "Mật khẩu quá ngắn, vui lòng nhập lại (ít nhất 8 ký tự).");
                return "redirect:/auth/register";
            }

            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Có lỗi xảy ra! Vui lòng thử lại.");
        }
        return "redirect:/auth/register";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}

