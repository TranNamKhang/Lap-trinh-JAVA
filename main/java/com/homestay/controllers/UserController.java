package com.homestay.controllers;

import com.homestay.models.User;
import com.homestay.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tên đăng nhập đã tồn tại!");
            }

            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email đã tồn tại!");
            }

            if (userService.existsByPhone(user.getPhone())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số điện thoại đã tồn tại!");
            }

            if (user.getPassword().length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu quá ngắn, ít nhất 8 ký tự.");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi trong quá trình đăng ký!");
        }
    }

}
