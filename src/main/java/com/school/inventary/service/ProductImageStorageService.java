package com.school.inventary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private final Path uploadDirectory;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    public ProductImageStorageService(
            @Value("${app.upload.product-dir:uploads/product}")
            String uploadDirectory) {

        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create product image directory", e
            );
        }
    }

    public String saveImage(MultipartFile image) {

        if (image == null || image.isEmpty()) {
            return null;
        }

        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Image size must not exceed 5 MB"
            );
        }

        String contentType = image.getContentType();

        if (contentType == null
                || !ALLOWED_TYPES.containsKey(
                contentType.toLowerCase())) {

            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are supported"
            );
        }

        String extension = ALLOWED_TYPES.get(
                contentType.toLowerCase()
        );

        String filename = UUID.randomUUID()
                .toString()
                .replace("-", "") + extension;

        Path destination = uploadDirectory.resolve(filename)
                .normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid image path"
            );
        }

        try {
            Files.copy(image.getInputStream(), destination);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save product image", e
            );
        }

        // This relative URL is stored in product_image_link.
        return "/uploads/product/" + filename;
    }

    public void deleteImage(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix = "/uploads/product/";

        // Only delete files managed by this service.
        if (!imageUrl.startsWith(prefix)) {
            return;
        }

        String filename = imageUrl.substring(prefix.length());

        // Reject paths containing directory traversal or separators.
        if (filename.isBlank()
                || filename.contains("/")
                || filename.contains("\\")
                || filename.equals(".")
                || filename.equals("..")) {
            return;
        }

        Path file = uploadDirectory.resolve(filename).normalize();

        if (!file.startsWith(uploadDirectory)) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to delete old product image", e
            );
        }
    }

    public Resource loadImageAsResource(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be blank");
        }

        String prefix = "/uploads/product/";
        String filename = imageUrl.startsWith(prefix) ? imageUrl.substring(prefix.length()) : imageUrl;

        // Reject paths containing directory traversal or separators.
        if (filename.isBlank()
                || filename.contains("/")
                || filename.contains("\\")
                || filename.equals(".")
                || filename.equals("..")) {
            throw new IllegalArgumentException("Invalid image filename");
        }

        Path file = uploadDirectory.resolve(filename).normalize();
        if (!file.startsWith(uploadDirectory) || !Files.exists(file)) {
            throw new IllegalArgumentException("Image file not found");
        }

        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Failed to load image file", e);
        }
    }

    public String determineContentType(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return "application/octet-stream";
        }
        String lower = imageUrl.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}