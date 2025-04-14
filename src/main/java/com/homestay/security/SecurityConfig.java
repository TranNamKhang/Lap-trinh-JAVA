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
        http.csrf(csrf -> csrf.disable()) 
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/guest/**", "/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() 
                        .requestMatchers("/uploads/images/**", "/uploads/avatars/**").permitAll() 
                        .requestMatchers("/homestays/**").permitAll() 
                        .requestMatchers("/user/homestays/api/**").permitAll() 
                        .requestMatchers("/error").permitAll() 

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/payment/manage", "/payment/confirm/**", "/payment/cancel/**").hasRole("ADMIN")

                        .requestMatchers("/user/home", "/user/profile/**", "/user/update-profile/**").authenticated()
                        .requestMatchers("/user/booking/**").authenticated() 
                        .requestMatchers("/payment/history/**", "/payment/info").authenticated() 
                        .requestMatchers("/payment/qr/**").authenticated() 

                        .anyRequest().authenticated() 
                )
                .formLogin(login -> login
                        .loginPage("/auth/login") 
                        .successHandler(new CustomAuthenticationSuccessHandler()) 
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler("/auth/login?error=true")) 
                        .permitAll()               )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") 
                        .logoutSuccessUrl("/auth/login?logout") 
                        .invalidateHttpSession(true) 
                        .deleteCookies("JSESSIONID") 
                        .permitAll() // Cho phép tất cả thực hiện logout
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession() 
                );

        return http.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");

       
        String userDir = System.getProperty("user.dir"); 
        String imageUploadPath = Paths.get(userDir, "uploads", "images").toUri().toString();
        String avatarUploadPath = Paths.get(userDir, "uploads", "avatars").toUri().toString();

       
        System.out.println("Serving images from: " + imageUploadPath); 
        System.out.println("Serving avatars from: " + avatarUploadPath); 

        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(imageUploadPath); 

        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarUploadPath); 
    }

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
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));

            if (isAdmin) {
                return "/admin/dashboard"; 
            } else {
                return "/user/home"; 
        }
    }
    }
}