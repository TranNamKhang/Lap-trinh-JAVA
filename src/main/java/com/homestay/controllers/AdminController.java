package com.homestay.controllers;

import com.homestay.models.Homestay;
import com.homestay.models.User;
import com.homestay.services.HomestayService;
import com.homestay.services.UserService;
import com.homestay.services.VisitCounterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private HomestayService homestayService;

    @Autowired
    private VisitCounterService visitCounterService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("visitorCount", visitCounterService.getTotalVisits());
        return "admin/dashboard";
    }
    public String adminDashboard(Model model) {
        model.addAttribute("visitorCount", visitCounterService.getTotalVisits());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/manage-user";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.findById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại!");
        } else {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng!");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user.get());
        return "admin/edit-user";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User updatedUser, RedirectAttributes redirectAttributes) {
        if (userService.findById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại!");
        } else {
            userService.updateUser(id, updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        }
        return "redirect:/admin/users";
    }

    // Quản lý Homestay
    @GetMapping("/homestays")
    public String listHomestays(Model model) {
        model.addAttribute("homestays", homestayService.getAllHomestays());
        return "admin/manage-homestay";
    }

    @GetMapping("/homestays/add")
    public String showAddHomestayForm(@RequestParam(value = "id", required = false) Long id, Model model) {
        model.addAttribute("homestay", id != null ? 
                homestayService.getHomestayById(id).orElse(new Homestay()) : 
                new Homestay());
        return "admin/manage-homestay-form";
    }

    @PostMapping("/homestays/save")
    public String saveHomestay(@ModelAttribute Homestay homestay,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam(value = "extraImageFiles", required = false) MultipartFile[] extraImageFiles,
                               RedirectAttributes redirectAttributes) {
        try {
            // Xử lý ảnh chính
            if (imageFile != null && !imageFile.isEmpty()) {
                String contentType = imageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Tệp ảnh chính không hợp lệ!");
                    return "redirect:/admin/homestays/add";
                }
                String imagePath = homestayService.saveImage(imageFile);
                homestay.setImage(imagePath);
            } else if (homestay.getId() != null) {
                homestayService.getHomestayById(homestay.getId()).ifPresent(h -> homestay.setImage(h.getImage()));
            }
    
            // Xử lý ảnh phụ
            List<String> extraImagePaths = new ArrayList<>();
    
            if (homestay.getId() != null) {
                homestayService.getHomestayById(homestay.getId()).ifPresent(h -> {
                    if (h.getExtraImages() != null) {
                        extraImagePaths.addAll(h.getExtraImages());
                    }
                });
            }
    
            if (extraImageFiles != null && extraImageFiles.length > 0) {
                for (MultipartFile file : extraImageFiles) {
                    if (!file.isEmpty()) {
                        String contentType = file.getContentType();
                        if (contentType != null && contentType.startsWith("image/")) {
                            String path = homestayService.saveImage(file);
                            extraImagePaths.add(path);
                        }
                    }
                }
            }
    
            homestay.setExtraImages(extraImagePaths);
    
            homestayService.createHomestay(homestay);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu homestay thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định: " + e.getMessage());
        }
    
        return "redirect:/admin/homestays";
    }

    @GetMapping("/homestays/delete/{id}")
    public String deleteHomestay(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (homestayService.getHomestayById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Homestay không tồn tại!");
        } else {
            homestayService.deleteHomestay(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa homestay!");
        }
        return "redirect:/admin/homestays";
    }

}
