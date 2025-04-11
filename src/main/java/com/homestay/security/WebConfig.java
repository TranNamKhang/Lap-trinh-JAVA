package com.homestay.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
    registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/"); // Đảm bảo tải JS
    registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
    registry.addResourceHandler("/uploads/images/**").addResourceLocations("file:D:/java/Lap-trinh-JAVA-main/uploads/images");
    registry.addResourceHandler("/uploads/avatars/**").addResourceLocations("file:D:/java/Lap-trinh-JAVA-main/uploads/avatars");
    }
}
