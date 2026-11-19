package com.school.inventary.controller;

import com.school.inventary.dto.ProductRequestDto;
import com.school.inventary.dto.ProductResponseDto;
import com.school.inventary.service.ProductService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/school/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    // =========================================
    // ADD MULTIPLE PRODUCTS WITH IMAGES
    // =========================================

    @PostMapping(
            value = {"", "/bulk"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public List<ProductResponseDto> addMultipleProducts(
            @RequestPart("products") String productsJson,
            @RequestPart(value = "images", required = false)
            List<MultipartFile> images,
            Authentication authentication) throws IOException {

        List<ProductRequestDto> requests;
        String trimmed = productsJson != null ? productsJson.trim() : "";
        if (trimmed.startsWith("[")) {
            requests = objectMapper.readValue(
                    trimmed,
                    new TypeReference<List<ProductRequestDto>>() {}
            );
        } else {
            ProductRequestDto single = objectMapper.readValue(
                    trimmed,
                    ProductRequestDto.class
            );
            requests = List.of(single);
        }

        // Programmatic Bean Validation for multipart JSON payloads
        Set<ConstraintViolation<ProductRequestDto>> allViolations = new HashSet<>();
        for (ProductRequestDto req : requests) {
            Set<ConstraintViolation<ProductRequestDto>> violations = validator.validate(req);
            allViolations.addAll(violations);
        }
        if (!allViolations.isEmpty()) {
            throw new ConstraintViolationException(allViolations);
        }

        return productService.addMultipleProducts(
                requests,
                images,
                authentication.getName()
        );
    }

    // =========================================
    // GET ALL ACTIVE PRODUCTS OR CATEGORY-WISE
    // =========================================

    @GetMapping
    public List<ProductResponseDto> getProducts(
            @RequestParam(required = false) String categoryId,
            Authentication authentication) {

        return productService.getProducts(
                categoryId,
                authentication.getName()
        );
    }

    // =========================================
    // GET SINGLE PRODUCT BY PRODUCT ID
    // =========================================

    @GetMapping("/{productId}")
    public ProductResponseDto getProduct(
            @PathVariable String productId,
            Authentication authentication) {

        return productService.getProductByProductId(
                productId,
                authentication.getName()
        );
    }

    // =========================================
    // GET PRODUCT IMAGE DIRECTLY
    // =========================================

    @GetMapping("/{productId}/image")
    public ResponseEntity<Resource> getProductImage(
            @PathVariable String productId) {

        return productService.getProductImageResource(productId);
    }

    // =========================================
    // UPDATE PRODUCT AND OPTIONALLY REPLACE IMAGE
    // =========================================

    @PutMapping(
            value = "/{productId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ProductResponseDto updateProduct(
            @PathVariable String productId,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false)
            MultipartFile image,
            Authentication authentication) throws IOException {

        ProductRequestDto request =
                objectMapper.readValue(
                        productJson,
                        ProductRequestDto.class
                );

        Set<ConstraintViolation<ProductRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return productService.updateProduct(
                productId,
                request,
                image,
                authentication.getName()
        );
    }

    // =========================================
    // DEACTIVATE PRODUCT
    // =========================================

    @DeleteMapping("/{productId}")
    public String deactivateProduct(
            @PathVariable String productId,
            Authentication authentication) {

        return productService.deactivateProduct(
                productId,
                authentication.getName()
        );
    }
}
