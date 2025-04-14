package com.homestay.services;

import com.homestay.models.User;
import com.homestay.models.Role;
import com.homestay.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Đọc giá trị đường dẫn từ application.properties hoặc có thể hardcode ở đây
    @Value("${upload.avatar.dir:D:/java/Lap-trinh-JAVA-main/uploads/avatars}")
    private String uploadDir;

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
        if (existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }

        validateUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            logger.info("🔄 Cập nhật thông tin người dùng ID: {}", id);

            if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
                user.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPhone() != null && !updatedUser.getPhone().isEmpty()) {
                user.setPhone(updatedUser.getPhone());
            }
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
    }

    public void updatePassword(Long id, String newPassword) {
        userRepository.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("🔑 Mật khẩu của người dùng ID: {} đã được cập nhật!", id);
        });
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Người dùng không tồn tại!");
        }
        userRepository.deleteById(id);
        logger.info("🗑️ Người dùng ID: {} đã bị xóa!", id);
    }

    @PostConstruct
    public void initAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = new User("admin", passwordEncoder.encode("123"), "tdanh589@gmail.com", "0123456789", Role.ADMIN);
            userRepository.save(user);
            logger.info("✅ Tài khoản admin tạo thành công!");
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    private void validateUser(User user) {
        if (user.getPassword().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu quá ngắn! Ít nhất 8 ký tự.");
        }
    }

    // Phương thức lưu ảnh avatar
    public String saveImage(MultipartFile file) throws IOException {
        // Lấy tên file và đặt tên mới
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        
        // Kiểm tra nếu thư mục upload không tồn tại thì tạo mới
        File uploadDirPath = new File(uploadDir);
        if (!uploadDirPath.exists()) {
            uploadDirPath.mkdirs();
        }

        // Lưu file vào thư mục
        Files.copy(file.getInputStream(), filePath);

        // Trả về đường dẫn tương đối để lưu trong cơ sở dữ liệu
        return "/uploads/avatars/" + fileName;
    }
}
