package com.homestay.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Paths; // Import Paths để xử lý đường dẫn nếu cần

@Configuration
@EnableMethodSecurity // Enable method-level security annotations
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        // ProviderManager chỉ cần một provider hoặc một danh sách
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Tắt CSRF (cân nhắc bật lại trong production với cấu hình phù hợp)
                .authorizeHttpRequests(auth -> auth
                        // --- Permit All ---
                        .requestMatchers("/", "/guest/**", "/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Static resources trong classpath
                        .requestMatchers("/uploads/images/**", "/uploads/avatars/**").permitAll() // Uploaded files (sẽ được cấu hình resource handler)
                        .requestMatchers("/homestays/**").permitAll() // Cho phép xem danh sách và chi tiết homestay
                        .requestMatchers("/user/homestays/api/**").permitAll() // API homestay cũng public
                        .requestMatchers("/error").permitAll() // Trang lỗi chung

                        // --- Admin Only ---
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/payment/manage", "/payment/confirm/**", "/payment/cancel/**").hasRole("ADMIN")

                        // --- Authenticated Users (ROLE_USER or ROLE_ADMIN) ---
                        .requestMatchers("/user/home", "/user/profile/**", "/user/update-profile/**").authenticated()
                        .requestMatchers("/user/booking/**").authenticated() // Đặt phòng, xem list booking của user
                        .requestMatchers("/payment/history/**", "/payment/info").authenticated() // Xem lịch sử, thông tin thanh toán của user
                        .requestMatchers("/payment/qr/**").authenticated() // Trang hiển thị QR code thanh toán của user

                        // --- Default Rule ---
                        .anyRequest().authenticated() // Tất cả các request khác yêu cầu phải đăng nhập
                )
                .formLogin(login -> login
                        .loginPage("/auth/login") // Trang đăng nhập tùy chỉnh
                        .successHandler(new CustomAuthenticationSuccessHandler()) // Xử lý sau khi đăng nhập thành công
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler("/auth/login?error=true")) // Xử lý khi đăng nhập thất bại
                        .permitAll() // Cho phép tất cả truy cập trang login
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // URL để kích hoạt logout
                        .logoutSuccessUrl("/auth/login?logout") // URL chuyển hướng sau khi logout thành công
                        .invalidateHttpSession(true) // Hủy bỏ session hiện tại
                        .deleteCookies("JSESSIONID") // Xóa cookie session (tùy chọn)
                        .permitAll() // Cho phép tất cả thực hiện logout
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession() // Bảo vệ chống tấn công session fixation
                );

        return http.build();
    }

    // Ghi đè ResourceHandlers để phục vụ file tĩnh và file upload
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Phục vụ file tĩnh từ thư mục resources/static
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");

        // --- PHỤC VỤ FILE UPLOAD ---
        // **QUAN TRỌNG:** Thay đổi các đường dẫn "/path/to/your/..." bằng đường dẫn tuyệt đối
        // đến thư mục chứa ảnh upload thực tế trên server của bạn.
        // Sử dụng tiền tố "file:" để chỉ định đường dẫn trên hệ thống file.

        // Ví dụ đường dẫn tuyệt đối (thay đổi cho phù hợp):
        String userDir = System.getProperty("user.dir"); // Lấy thư mục gốc của dự án
        String imageUploadPath = Paths.get(userDir, "uploads", "images").toUri().toString();
        String avatarUploadPath = Paths.get(userDir, "uploads", "avatars").toUri().toString();

        // Hoặc chỉ định đường dẫn cứng (thay đổi cho phù hợp):
        // String imageUploadPath = "file:/var/www/homestay/uploads/images/";
        // String avatarUploadPath = "file:/var/www/homestay/uploads/avatars/";

        System.out.println("Serving images from: " + imageUploadPath); // Log để kiểm tra đường dẫn
        System.out.println("Serving avatars from: " + avatarUploadPath); // Log để kiểm tra đường dẫn

        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(imageUploadPath); // Đường dẫn đến thư mục images upload

        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarUploadPath); // Đường dẫn đến thư mục avatars upload
    }

    // Handler chuyển hướng sau khi đăng nhập thành công dựa trên role
    public static class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {

            String targetUrl = determineTargetUrl(authentication);

            if (response.isCommitted()) {
                logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
                return;
            }

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }

        protected String determineTargetUrl(Authentication authentication) {
            // Kiểm tra xem người dùng có quyền ADMIN không
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));

            if (isAdmin) {
                return "/admin/dashboard"; // Hoặc trang mặc định của admin
            } else {
                return "/user/home"; // Trang mặc định cho người dùng thường
            }
        }
    }
}