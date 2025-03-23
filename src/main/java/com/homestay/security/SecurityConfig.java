package com.homestay.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import java.util.List;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        return new ProviderManager(List.of(configureAuthenticationProvider(userDetailsService)));
    }

    private DaoAuthenticationProvider configureAuthenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/guest/**", "/auth/login", "/auth/register").permitAll()
                .requestMatchers("/css/**", "/js/**", "/image/**", "/uploads/images/**", "/uploads/avatars/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**", "/user/profile/**", "/user/update-profile/**").authenticated()
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
            .sessionManagement(session -> session.sessionFixation().none());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/css/**", "/js/**", "/image/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        addStaticResourceHandlers(registry, "/css/**", "classpath:/static/css/");
        addStaticResourceHandlers(registry, "/js/**", "classpath:/static/js/");
        addStaticResourceHandlers(registry, "/image/**", "classpath:/static/image/");
        addStaticResourceHandlers(registry, "/uploads/images/**", "file:uploads/images/");
        addStaticResourceHandlers(registry, "/uploads/avatars/**", "file:uploads/avatars/");
    }

    private void addStaticResourceHandlers(ResourceHandlerRegistry registry, String pattern, String location) {
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }

    public static class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
        @Override
        protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
            getRedirectStrategy().sendRedirect(request, response, determineRedirect(authentication));
        }

        private String determineRedirect(Authentication authentication) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN")) ? "/admin" : "/user/home";
        }
    }
}
