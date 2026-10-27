package com.school.inventary.controller;

import com.school.inventary.dto.CategoryRequestDto;
import com.school.inventary.dto.CategoryResponseDto;
import com.school.inventary.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/add_category")
    public List<CategoryResponseDto> addMultipleCategories(
            @Valid @RequestBody List<@Valid CategoryRequestDto> requests,
            Authentication authentication) {

        return categoryService.addMultipleCategories(
                requests,
                authentication.getName()
        );
    }

    // Show active categories in descending order
    @GetMapping("/get_All_Category")
    public List<CategoryResponseDto> getCategories(
            Authentication authentication) {

        return categoryService.getActiveCategories(
                authentication.getName()
        );
    }




    @PutMapping("/{categoryId}")
    public CategoryResponseDto updateCategory(
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryRequestDto request,
            Authentication authentication) {

        return categoryService.updateCategory(
                categoryId, request, authentication.getName()
        );
    }

    @DeleteMapping("/{categoryId}")
    public String deactivateCategory(
            @PathVariable String categoryId,
            Authentication authentication) {

        categoryService.deactivateCategory(
                categoryId, authentication.getName()
        );

        return "Category deactivated successfully";
    }

}