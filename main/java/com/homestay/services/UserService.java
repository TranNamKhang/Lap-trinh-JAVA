package com.homestay.services;

import com.homestay.models.User;
import com.homestay.models.Role;
import com.homestay.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        validateUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Mã hóa mật khẩu

        // Đặt role mặc định nếu chưa có
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        User savedUser = userRepository.save(user);
        logger.info("✅ Người dùng '{}' đã được đăng ký thành công!", user.getUsername());
        return savedUser;
    }

    public List<User> getAllUsers() {
        logger.info("📌 Lấy danh sách tất cả người dùng");
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        logger.info("📌 Tìm người dùng với ID: {}", id);
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        logger.info("📌 Tìm người dùng với email: {}", email);
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        logger.info("📌 Tìm người dùng với username: {}", username);
        return userRepository.findByUsername(username);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            logger.info("🔄 Cập nhật thông tin người dùng ID: {}", id);
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());

            // Đảm bảo không cập nhật role thành null
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }

            return userRepository.save(user);
        }).orElseThrow(() -> {
            logger.error("❌ Không tìm thấy người dùng ID: {}", id);
            return new IllegalArgumentException("Không tìm thấy người dùng!");
        });
    }

    public void updatePassword(Long id, String newPassword) {
        userRepository.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("🔑 Mật khẩu của người dùng ID: {} đã được cập nhật!", id);
        });
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            logger.error("❌ Không tìm thấy người dùng ID: {}", id);
            throw new IllegalArgumentException("Người dùng không tồn tại!");
        }
        userRepository.deleteById(id);
        logger.info("🗑️ Người dùng ID: {} đã bị xóa!", id);
    }

    @PostConstruct
    public void initAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = new User("admin", passwordEncoder.encode("123"), "admin@example.com", Role.ADMIN);
            userRepository.save(user);
            logger.info("✅ Tài khoản admin đã được tạo thành công!");
        } else {
            logger.info("⚠️ Tài khoản admin đã tồn tại, không cần tạo lại!");
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateUser(User user) {
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            throw new IllegalArgumentException("Tên đăng nhập phải có từ 3 đến 20 ký tự!");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }
    }
}
