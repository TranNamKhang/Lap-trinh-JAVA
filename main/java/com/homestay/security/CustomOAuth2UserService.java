package com.homestay.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.homestay.models.CustomOAuth2User;
import com.homestay.models.User;
import com.homestay.models.Role;
import com.homestay.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2User);
        
        // Lưu hoặc cập nhật thông tin người dùng vào database
        String email = customOAuth2User.getEmail();
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isEmpty()) {
            // Tạo người dùng mới nếu chưa tồn tại
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email); // Sử dụng email làm username
            newUser.setPassword(passwordEncoder.encode("google_oauth2_password")); // Mật khẩu mặc định
            newUser.setAvatar(customOAuth2User.getPicture());
            newUser.setRole(Role.USER); // Mặc định là USER
            newUser.setOauthProvider("google");
            userRepository.save(newUser);
        }
        
        return customOAuth2User;
    }
} 