package com.school.inventary.service;

import com.school.inventary.dto.ProductRequestDto;
import com.school.inventary.dto.ProductResponseDto;
import com.school.inventary.entity.CategoryEntity;
import com.school.inventary.entity.ProductEntity;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.CategoryRepository;
import com.school.inventary.repo.ProductRepository;
import com.school.inventary.repo.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductImageStorageService imageStorageService;

    // =========================================
    // ADD MULTIPLE PRODUCTS
    // =========================================

    @Transactional
    public List<ProductResponseDto> addMultipleProducts(
            List<ProductRequestDto> requests,
            List<MultipartFile> images,
            String loggedInEmail) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one product is required"
            );
        }

        UserEntity school = getActiveSchool(loggedInEmail);
        String masterId = school.getMasterId();

        ProductRequestDto first = requests.get(0);

        if (first == null
                || first.getCategoryId() == null
                || first.getCategoryId().isBlank()) {
            throw new IllegalArgumentException(
                    "Category ID is required"
            );
        }

        String categoryId = first.getCategoryId().trim();

        Set<String> productNames = new HashSet<>();

        // Validate the entire batch before writing files or records.
        for (ProductRequestDto request : requests) {

            validateProductRequest(request);

            if (!categoryId.equals(request.getCategoryId().trim())) {
                throw new IllegalArgumentException(
                        "All products must belong to the same category"
                );
            }

            String normalizedName = request.getProductName()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!productNames.add(normalizedName)) {
                throw new IllegalArgumentException(
                        "Duplicate product in request: "
                                + request.getProductName()
                );
            }

            validateImageIndex(request, images);

            boolean exists = productRepository
                    .existsByMasterIdAndCategoryIdAndProductNameIgnoreCaseAndStatus(
                            masterId,
                            categoryId,
                            request.getProductName().trim(),
                            1
                    );

            if (exists) {
                throw new IllegalArgumentException(
                        "Active product already exists: "
                                + request.getProductName()
                );
            }
        }

        CategoryEntity category =
                getActiveCategory(categoryId, masterId);

        List<ProductResponseDto> responses = new ArrayList<>();

        for (ProductRequestDto request : requests) {

            ProductEntity product = new ProductEntity();

            // Always create a new entity, including when an
            // inactive product with the same name already exists.
            product.setMasterId(masterId);
            product.setCategoryId(category.getCategoryId());
            product.setProductName(request.getProductName().trim());
            product.setStatus(1);

            applyRequest(product, request);

            MultipartFile image = getImage(request, images);

            if (image != null && !image.isEmpty()) {
                product.setProductImageLink(
                        imageStorageService.saveImage(image)
                );
            }

            ProductEntity saved = productRepository.save(product);

            responses.add(mapToDto(saved));
        }

        return responses;
    }

    // =========================================
    // GET ALL ACTIVE PRODUCTS OR CATEGORY-WISE
    // =========================================

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(
            String categoryId,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);
        String masterId = school.getMasterId();

        List<ProductEntity> products;

        if (categoryId != null && !categoryId.isBlank()) {

            String normalizedCategoryId = categoryId.trim();

            getActiveCategory(normalizedCategoryId, masterId);

            products = productRepository
                    .findByMasterIdAndCategoryIdAndStatusOrderByCreatedAtDesc(
                            masterId,
                            normalizedCategoryId,
                            1
                    );

        } else {
            products = productRepository
                    .findByMasterIdAndStatusOrderByCreatedAtDesc(
                            masterId,
                            1
                    );
        }

        return products.stream()
                .map(this::mapToDto)
                .toList();
    }

    // =========================================
    // UPDATE PRODUCT
    // =========================================

    @Transactional
    public ProductResponseDto updateProduct(
            String productId,
            ProductRequestDto request,
            MultipartFile newImage,
            String loggedInEmail) {

        validateProductRequest(request);

        UserEntity school = getActiveSchool(loggedInEmail);
        String masterId = school.getMasterId();

        ProductEntity product = productRepository
                .findByProductIdAndMasterId(productId, masterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found"
                ));

        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new IllegalArgumentException(
                    "Inactive products cannot be updated"
            );
        }

        String categoryId = request.getCategoryId().trim();

        CategoryEntity category =
                getActiveCategory(categoryId, masterId);

        String productName = request.getProductName().trim();

        boolean duplicate = productRepository
                .existsByMasterIdAndCategoryIdAndProductNameIgnoreCaseAndProductIdNotAndStatus(
                        masterId,
                        category.getCategoryId(),
                        productName,
                        productId,
                        1
                );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Another active product already uses this name"
            );
        }

        String oldImage = product.getProductImageLink();

        product.setCategoryId(category.getCategoryId());
        applyRequest(product, request);

        if (newImage != null && !newImage.isEmpty()) {
            product.setProductImageLink(
                    imageStorageService.saveImage(newImage)
            );
        }

        ProductEntity saved = productRepository.save(product);

        // If an image was replaced, remove the previous managed file.
        if (newImage != null && !newImage.isEmpty()
                && oldImage != null
                && !oldImage.equals(saved.getProductImageLink())) {
            imageStorageService.deleteImage(oldImage);
        }

        return mapToDto(saved);
    }

    // =========================================
    // SOFT DELETE PRODUCT
    // =========================================

    @Transactional
    public String deactivateProduct(
            String productId,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);

        ProductEntity product = productRepository
                .findByProductIdAndMasterId(
                        productId,
                        school.getMasterId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found"
                ));

        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new IllegalArgumentException(
                    "Product is already inactive"
            );
        }

        // Keep the database record and image file.
        product.setStatus(0);
        productRepository.save(product);

        return "Product deactivated successfully";
    }

    // =========================================
    // VALIDATE REQUEST
    // =========================================

    private void validateProductRequest(ProductRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Product details are required"
            );
        }

        if (request.getCategoryId() == null
                || request.getCategoryId().isBlank()) {
            throw new IllegalArgumentException(
                    "Category ID is required"
            );
        }

        if (request.getProductName() == null
                || request.getProductName().isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (request.getUom() == null
                || request.getUom().isBlank()) {
            throw new IllegalArgumentException(
                    "UOM is required"
            );
        }

        if (request.getMinQuantity() == null
                || request.getMinQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Minimum quantity must be zero or greater"
            );
        }
    }

    private void validateImageIndex(
            ProductRequestDto request,
            List<MultipartFile> images) {

        Integer index = request.getImageIndex();

        if (index == null) {
            return;
        }

        if (images == null || index < 0 || index >= images.size()) {
            throw new IllegalArgumentException(
                    "Invalid imageIndex for product: "
                            + request.getProductName()
            );
        }
    }

    private MultipartFile getImage(
            ProductRequestDto request,
            List<MultipartFile> images) {

        if (request.getImageIndex() == null || images == null) {
            return null;
        }

        return images.get(request.getImageIndex());
    }

    // =========================================
    // SCHOOL VALIDATION
    // =========================================

    private UserEntity getActiveSchool(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Logged-in user not found"
                ));

        // Project convention: role 0 = ADMIN, role 1 = SCHOOL
        if (!Integer.valueOf(1).equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "Only school accounts can manage products"
            );
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "School account is inactive"
            );
        }

        return user;
    }

    // =========================================
    // CATEGORY VALIDATION
    // =========================================

    private CategoryEntity getActiveCategory(
            String categoryId,
            String masterId) {

        CategoryEntity category = categoryRepository
                .findByCategoryIdAndMasterId(categoryId, masterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found for this school"
                ));

        if (!Integer.valueOf(1).equals(category.getStatus())) {
            throw new IllegalArgumentException(
                    "Category is inactive"
            );
        }

        return category;
    }

    // =========================================
    // APPLY PRODUCT DATA
    // =========================================

    private void applyRequest(
            ProductEntity product,
            ProductRequestDto request) {

        product.setProductName(request.getProductName().trim());
        product.setUom(request.getUom().trim());
        product.setProductDescription(request.getProductDescription());
        product.setMinQuantity(request.getMinQuantity());
        product.setRackDetail(request.getRackDetail());
    }

    // =========================================
    // GET SINGLE PRODUCT BY ID
    // =========================================

    @Transactional(readOnly = true)
    public ProductResponseDto getProductByProductId(
            String productId,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);
        ProductEntity product = productRepository
                .findByProductIdAndMasterId(productId, school.getMasterId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new IllegalArgumentException("Product is inactive");
        }

        return mapToDto(product);
    }

    // =========================================
    // GET PRODUCT IMAGE RESOURCE
    // =========================================

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getProductImageResource(String productId) {
        ProductEntity product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getProductImageLink() == null || product.getProductImageLink().isBlank()) {
            throw new IllegalArgumentException("Product does not have an image");
        }

        Resource resource = imageStorageService.loadImageAsResource(product.getProductImageLink());
        String contentType = imageStorageService.determineContentType(product.getProductImageLink());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(resource);
    }

    // =========================================
    // ENTITY TO RESPONSE DTO
    // =========================================

    private ProductResponseDto mapToDto(ProductEntity product) {

        String fullImageUrl = null;
        if (product.getProductImageLink() != null && !product.getProductImageLink().isBlank()) {
            try {
                fullImageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(product.getProductImageLink())
                        .toUriString();
            } catch (Exception e) {
                fullImageUrl = product.getProductImageLink();
            }
        }

        return ProductResponseDto.builder()
                .id(product.getId())
                .masterId(product.getMasterId())
                .categoryId(product.getCategoryId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .uom(product.getUom())
                .productImageLink(product.getProductImageLink())
                .productImageUrl(fullImageUrl)
                .productDescription(product.getProductDescription())
                .minQuantity(product.getMinQuantity())
                .rackDetail(product.getRackDetail())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }
}