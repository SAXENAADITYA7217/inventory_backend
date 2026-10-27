package com.school.inventary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ProductImageConfig implements WebMvcConfigurer {

    @Value("${app.upload.product-dir:uploads/product}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        Path directory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        String location = directory.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler("/uploads/product/**")
                .addResourceLocations(location);

        Path parentDir = directory.getParent() != null ? directory.getParent() : directory;
        String parentLocation = parentDir.toUri().toString();
        if (!parentLocation.endsWith("/")) {
            parentLocation += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(parentLocation);
    }
}