package com.mediscan.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        String uploadLocation = Paths
                .get(uploadDir)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        System.out.println(
                "WEB IMAGE DIRECTORY: " + uploadLocation
        );

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}