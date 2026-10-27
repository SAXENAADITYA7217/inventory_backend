package com.school.inventary.service;


import com.school.inventary.dto.CategoryRequestDto;
import com.school.inventary.dto.CategoryResponseDto;
import com.school.inventary.entity.CategoryEntity;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.entity.ProductEntity;
import com.school.inventary.repo.CategoryRepository;
import com.school.inventary.repo.ProductRepository;
import com.school.inventary.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageStorageService imageStorageService;


    @Transactional
    public List<CategoryResponseDto> addMultipleCategories(
            List<CategoryRequestDto> requests,
            String loggedInEmail) {

        UserEntity school = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new IllegalArgumentException(
                    "Only school accounts can add categories"
            );
        }

        if (school.getStatus() == null || school.getStatus() != 1) {
            throw new IllegalArgumentException(
                    "School account is inactive"
            );
        }

        String masterId = school.getMasterId();

        List<CategoryResponseDto> responses = new ArrayList<>();
        Set<String> processedNames = new HashSet<>();

        for (CategoryRequestDto request : requests) {

            String categoryName = request.getCategoryName().trim();

            if (categoryName.isEmpty()) {
                throw new IllegalArgumentException(
                        "Category name cannot be empty"
                );
            }

            // Avoid duplicate names in the same request
            String normalizedName = categoryName.toLowerCase(Locale.ROOT);

            if (!processedNames.add(normalizedName)) {
                continue;
            }

            // Check if an active category with this name already exists
            boolean activeExists = categoryRepository
                    .existsByMasterIdAndCategoryNameIgnoreCaseAndStatus(
                            masterId,
                            categoryName,
                            1
                    );

            if (activeExists) {
                throw new IllegalArgumentException(
                        "Active category already exists: " + categoryName
                );
            }

            // Always create a brand new category (not associated with any previous inactive one)
            CategoryEntity newCategory = new CategoryEntity();

            newCategory.setMasterId(masterId);
            newCategory.setCategoryId(
                    "CAT-" + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .toUpperCase(Locale.ROOT)
            );
            newCategory.setCategoryName(categoryName);
            newCategory.setStatus(1);

            CategoryEntity savedCategory =
                    categoryRepository.save(newCategory);

            responses.add(mapToResponse(savedCategory));
        }

        return responses;
    }






// CategoryService class ke andar:

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getActiveCategories(String loggedInEmail) {

        UserEntity user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == null || user.getRole() != 1) {
            throw new IllegalArgumentException("Only school users can view categories");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("School account is inactive");
        }

        return categoryRepository
                .findByMasterIdAndStatusOrderByCreatedAtDesc(
                        user.getMasterId(), 1
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CategoryResponseDto updateCategory(
            Long id,
            CategoryRequestDto request,
            String loggedInEmail
    ) {
        UserEntity user = getActiveSchool(loggedInEmail);

        if (request == null || request.getCategoryName() == null
                || request.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        String newName = request.getCategoryName().trim();

        if (newName.length() > 150) {
            throw new IllegalArgumentException(
                    "Category name cannot exceed 150 characters"
            );
        }

        CategoryEntity category = categoryRepository
                .findByIdAndMasterId(id, user.getMasterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found"
                ));

        if (category.getStatus() != 1) {
            throw new IllegalArgumentException("Inactive category cannot be edited");
        }

        boolean duplicate = categoryRepository
                .existsByMasterIdAndCategoryNameIgnoreCaseAndIdNotAndStatus(
                        user.getMasterId(),
                        newName,
                        id,
                        1
                );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "This category name already exists"
            );
        }

        // Sirf name update hoga
        category.setCategoryName(newName);

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deactivateCategory(Long id, String loggedInEmail) {

        UserEntity user = getActiveSchool(loggedInEmail);

        CategoryEntity category = categoryRepository
                .findByIdAndMasterId(id, user.getMasterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found"
                ));

        if (category.getStatus() != 1) {
            throw new IllegalArgumentException("Category is already inactive");
        }

        // Deactivate all associated products (set status = 0)
        List<ProductEntity> associatedProducts = productRepository
                .findByMasterIdAndCategoryId(user.getMasterId(), category.getCategoryId());

        for (ProductEntity prod : associatedProducts) {
            prod.setStatus(0);
        }

        if (!associatedProducts.isEmpty()) {
            productRepository.saveAll(associatedProducts);
        }

        // Soft delete category
        category.setStatus(0);
        categoryRepository.save(category);
    }

    private UserEntity getActiveSchool(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == null || user.getRole() != 1) {
            throw new IllegalArgumentException("Only school users can manage categories");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("School account is inactive");
        }

        return user;
    }



    @Transactional
    public CategoryResponseDto updateCategory(
            String categoryId,
            CategoryRequestDto request,
            String loggedInEmail
    ) {
        UserEntity user = getActiveSchool(loggedInEmail);

        if (request == null || request.getCategoryName() == null
                || request.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        String newName = request.getCategoryName().trim();

        if (newName.length() > 150) {
            throw new IllegalArgumentException(
                    "Category name cannot exceed 150 characters"
            );
        }

        CategoryEntity category = categoryRepository
                .findByCategoryIdAndMasterId(
                        categoryId, user.getMasterId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found"
                ));

        if (category.getStatus() != 1) {
            throw new IllegalArgumentException(
                    "Inactive category cannot be edited"
            );
        }

        boolean duplicate = categoryRepository
                .existsByMasterIdAndCategoryNameIgnoreCaseAndCategoryIdNotAndStatus(
                        user.getMasterId(),
                        newName,
                        categoryId,
                        1
                );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "This category name already exists"
            );
        }

        category.setCategoryName(newName);

        return mapToResponse(categoryRepository.save(category));
    }


    @Transactional
    public void deactivateCategory(
            String categoryId,
            String loggedInEmail
    ) {
        UserEntity user = getActiveSchool(loggedInEmail);

        CategoryEntity category = categoryRepository
                .findByCategoryIdAndMasterId(
                        categoryId, user.getMasterId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found"
                ));

        if (category.getStatus() != 1) {
            throw new IllegalArgumentException(
                    "Category is already inactive"
            );
        }

        // Deactivate all associated products (status = 0)
        List<ProductEntity> associatedProducts = productRepository
                .findByMasterIdAndCategoryId(user.getMasterId(), category.getCategoryId());

        for (ProductEntity prod : associatedProducts) {
            prod.setStatus(0);
        }

        if (!associatedProducts.isEmpty()) {
            productRepository.saveAll(associatedProducts);
        }

        category.setStatus(0);
        categoryRepository.save(category);
    }




    private CategoryResponseDto mapToResponse(CategoryEntity category){
        return CategoryResponseDto.builder()
                .id(category.getId())
                .masterId(category.getMasterId())
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .build();
    }

}


