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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(List.of(authProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/guest/**", "/auth/login", "/auth/register").permitAll()  
                .requestMatchers("/css/**", "/js/**", "/image/**", "/uploads/images/**").permitAll()
                .requestMatchers("/uploads/avatars/**").permitAll() // Thêm quyền truy cập cho ảnh đại diện
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").authenticated() // Changed to authenticated()
                .requestMatchers("/user/profile/**","/user/update-profile/**").authenticated()
                .requestMatchers("/fragments/navbar/**").authenticated()

            )
            .formLogin(login -> login
                .loginPage("/auth/login")
                .successHandler(new CustomAuthenticationSuccessHandler())
                .failureHandler(new SimpleUrlAuthenticationFailureHandler("/auth/login?error=true"))
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
                .invalidateHttpSession(false) 
                )
                .sessionManagement(session -> session
                    .sessionFixation().none() 
                );
        
            return http.build();
        }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/image/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/image/**")
                .addResourceLocations("classpath:/static/image/");
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:uploads/images/");
        registry.addResourceHandler("/uploads/avatars/**")  // Thêm quyền truy cập ảnh đại diện
                .addResourceLocations("file:uploads/avatars/");  // Đảm bảo đường dẫn đến thư mục ảnh đại diện
    }

    public static class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
        @Override
        protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
            String redirectUrl = determineRedirect(authentication);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }

        private String determineRedirect(Authentication authentication) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    return "/admin"; // Redirect to the admin page
                } else if (authority.getAuthority().equals("ROLE_USER")) {
                    return "/user/home"; // Redirect to the user home page
                }
            }
            return "/"; // Default redirect if no specific role is found
        }
    }
}
