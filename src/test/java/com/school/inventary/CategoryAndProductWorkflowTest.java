package com.school.inventary;

import com.school.inventary.dto.CategoryRequestDto;
import com.school.inventary.dto.CategoryResponseDto;
import com.school.inventary.dto.ProductRequestDto;
import com.school.inventary.dto.ProductResponseDto;
import com.school.inventary.entity.CategoryEntity;
import com.school.inventary.entity.ProductEntity;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.CategoryRepository;
import com.school.inventary.repo.ProductRepository;
import com.school.inventary.repo.UserRepository;
import com.school.inventary.service.CategoryService;
import com.school.inventary.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryAndProductWorkflowTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private String schoolEmail;
    private String masterId;

    @BeforeEach
    void setup() {
        masterId = "MST_" + UUID.randomUUID().toString().substring(0, 8);
        schoolEmail = "school_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";

        UserEntity school = new UserEntity();
        school.setEmail(schoolEmail);
        school.setMasterId(masterId);
        school.setName("Test School");
        school.setSchoolName("Test Academy");
        school.setPhoneNumber("9999999999");
        school.setPassword("password123");
        school.setRole(1); // 1 = SCHOOL
        school.setStatus(1); // 1 = ACTIVE
        userRepository.save(school);
    }

    @Test
    void testCategoryDeactivationCascadesToProductsAndRecreationIsNew() {
        // 1. Add Category
        CategoryRequestDto catReq = new CategoryRequestDto();
        catReq.setCategoryName("Science Lab");
        List<CategoryResponseDto> catList = categoryService.addMultipleCategories(List.of(catReq), schoolEmail);
        assertEquals(1, catList.size());
        CategoryResponseDto firstCategory = catList.get(0);
        String firstCatId = firstCategory.getCategoryId();
        assertEquals(1, firstCategory.getStatus());

        // 2. Add Product to this Category
        ProductRequestDto prodReq = new ProductRequestDto();
        prodReq.setCategoryId(firstCatId);
        prodReq.setProductName("Microscope");
        prodReq.setUom("PCS");
        prodReq.setMinQuantity(2);
        prodReq.setRackDetail("R1");

        List<ProductResponseDto> prodList = productService.addMultipleProducts(List.of(prodReq), null, schoolEmail);
        assertEquals(1, prodList.size());
        ProductResponseDto product1 = prodList.get(0);
        assertEquals(1, product1.getStatus());
        assertEquals(firstCatId, product1.getCategoryId());

        // 3. Deactivate the Category
        categoryService.deactivateCategory(firstCatId, schoolEmail);

        // Verify Category is inactive
        CategoryEntity updatedCategory = categoryRepository.findByCategoryId(firstCatId).orElseThrow();
        assertEquals(0, updatedCategory.getStatus());

        // Verify associated Product is automatically inactive (status = 0) and NOT hard-deleted
        ProductEntity updatedProduct = productRepository.findByProductId(product1.getProductId()).orElseThrow();
        assertEquals(0, updatedProduct.getStatus());

        // Verify active products list is now empty
        List<ProductResponseDto> activeProducts = productService.getProducts(null, schoolEmail);
        assertTrue(activeProducts.isEmpty());

        // 4. Re-add category with the SAME NAME ("Science Lab")
        CategoryRequestDto reAddCatReq = new CategoryRequestDto();
        reAddCatReq.setCategoryName("Science Lab");
        List<CategoryResponseDto> reAddedCats = categoryService.addMultipleCategories(List.of(reAddCatReq), schoolEmail);
        assertEquals(1, reAddedCats.size());
        CategoryResponseDto newCategory = reAddedCats.get(0);

        // Verify new category is ACTIVE, but has a DIFFERENT categoryId (brand new entity)
        assertEquals(1, newCategory.getStatus());
        assertNotEquals(firstCatId, newCategory.getCategoryId());

        // Verify products under new category is empty (not associated with old one)
        List<ProductResponseDto> newCatProducts = productService.getProducts(newCategory.getCategoryId(), schoolEmail);
        assertTrue(newCatProducts.isEmpty());

        // 5. Add product with SAME NAME ("Microscope") to new category
        ProductRequestDto newProdReq = new ProductRequestDto();
        newProdReq.setCategoryId(newCategory.getCategoryId());
        newProdReq.setProductName("Microscope");
        newProdReq.setUom("PCS");
        newProdReq.setMinQuantity(5);

        List<ProductResponseDto> newProdList = productService.addMultipleProducts(List.of(newProdReq), null, schoolEmail);
        assertEquals(1, newProdList.size());
        ProductResponseDto newProduct = newProdList.get(0);

        assertEquals(1, newProduct.getStatus());
        assertNotEquals(product1.getProductId(), newProduct.getProductId());
        assertEquals(newCategory.getCategoryId(), newProduct.getCategoryId());
    }
}
