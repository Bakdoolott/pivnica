package com.github.bakdoolott.coreservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${storage.payment-qr-dir:./uploads/payment-qr}")
    private String qrDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolute = Paths.get(qrDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/payment-qr/**")
                .addResourceLocations(absolute);
    }
}