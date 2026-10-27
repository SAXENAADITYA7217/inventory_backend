
package com.school.inventary.service;

import com.school.inventary.dto.ComboItemRequestDto;
import com.school.inventary.dto.ComboItemResponseDto;
import com.school.inventary.dto.ComboRequestDto;
import com.school.inventary.dto.ComboResponseDto;
import com.school.inventary.entity.ComboEntity;
import com.school.inventary.entity.ComboItemEntity;
import com.school.inventary.entity.ProductEntity;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.ComboItemRepository;
import com.school.inventary.repo.ComboRepository;
import com.school.inventary.repo.ProductRepository;
import com.school.inventary.repo.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ComboService {

    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Create a new combo
    @Transactional
    public ComboResponseDto createCombo(
            ComboRequestDto request,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);
        String masterId = school.getMasterId();

        validateRequest(request);

        String comboName = request.getComboName().trim();

        // Check duplicate active combo name
        if (comboRepository
                .existsByMasterIdAndComboNameIgnoreCaseAndStatus(
                        masterId, comboName, 1)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Combo name already exists"
            );
        }

        // Prevent duplicate products in the same request
        Set<String> productIds = new HashSet<>();

        for (ComboItemRequestDto item : request.getItems()) {

            String productId = item.getProductId().trim();

            if (!productIds.add(productId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate product ID: " + productId
                );
            }

            if (item.getQuantity() == null ||
                    item.getQuantity() < 1) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product quantity must be at least 1"
                );
            }

            // Product must belong to this school and be active
            ProductEntity product = productRepository
                    .findByProductIdAndMasterId(
                            productId, masterId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Product not found: " + productId
                            ));

            if (product.getStatus() == null ||
                    product.getStatus() != 1) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product is inactive: " + productId
                );
            }
        }

        // Save main combo
        ComboEntity combo = new ComboEntity();
        combo.setMasterId(masterId);
        combo.setComboName(comboName);
        combo.setStatus(1);

        combo = comboRepository.save(combo);

        // Save products belonging to this combo
        List<ComboItemEntity> savedItems = new ArrayList<>();

        for (ComboItemRequestDto item : request.getItems()) {

            ComboItemEntity comboItem = new ComboItemEntity();

            comboItem.setMasterId(masterId);
            comboItem.setComboId(combo.getComboId());
            comboItem.setProductId(item.getProductId().trim());
            comboItem.setQuantity(item.getQuantity());

            savedItems.add(comboItem);
        }

        comboItemRepository.saveAll(savedItems);

        return mapToResponse(combo, masterId);
    }

    // Get all active combos for the logged-in school
    @Transactional(readOnly = true)
    public List<ComboResponseDto> getAllCombos(
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);
        String masterId = school.getMasterId();

        return comboRepository
                .findByMasterIdAndStatusOrderByCreatedAtDesc(
                        masterId, 1)
                .stream()
                .map(combo -> mapToResponse(combo, masterId))
                .toList();
    }

    // Get one combo with its products
    @Transactional(readOnly = true)
    public ComboResponseDto getComboById(
            String comboId,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);

        ComboEntity combo = comboRepository
                .findByComboIdAndMasterId(
                        comboId, school.getMasterId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Combo not found"
                        ));

        if (combo.getStatus() == null ||
                combo.getStatus() != 1) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Active combo not found"
            );
        }

        return mapToResponse(combo, school.getMasterId());
    }



    @Transactional
    public ComboResponseDto updateCombo(
            String comboId,
            ComboRequestDto request,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);
        String masterId = school.getMasterId();

        validateRequest(request);

        ComboEntity combo = comboRepository
                .findByComboIdAndMasterId(comboId, masterId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Combo not found"
                        ));

        if (combo.getStatus() == null || combo.getStatus() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Active combo not found"
            );
        }

        String comboName = request.getComboName().trim();

        // Check duplicate active combo name
        boolean duplicateName =
                comboRepository
                        .existsByMasterIdAndComboNameIgnoreCaseAndStatus(
                                masterId, comboName, 1)
                        && !combo.getComboName().equalsIgnoreCase(comboName);

        if (duplicateName) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Combo name already exists"
            );
        }

        // Validate products and reject duplicates
        Set<String> requestedProductIds = new HashSet<>();

        for (ComboItemRequestDto item : request.getItems()) {

            if (item.getProductId() == null ||
                    item.getProductId().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product ID is required"
                );
            }

            String productId = item.getProductId().trim();

            if (!requestedProductIds.add(productId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate product ID: " + productId
                );
            }

            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product quantity must be at least 1"
                );
            }

            ProductEntity product = productRepository
                    .findByProductIdAndMasterId(productId, masterId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Product not found: " + productId
                            ));

            if (product.getStatus() == null ||
                    product.getStatus() != 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product is inactive: " + productId
                );
            }
        }

        // Update combo name
        combo.setComboName(comboName);
        comboRepository.save(combo);

        // Fetch existing combo items
        List<ComboItemEntity> existingItems =
                comboItemRepository
                        .findByMasterIdAndComboIdOrderByIdAsc(
                                masterId, comboId);

        // Create a map of existing items by product ID
        Map<String, ComboItemEntity> existingMap =
                existingItems.stream()
                        .collect(Collectors.toMap(
                                ComboItemEntity::getProductId,
                                item -> item
                        ));

        List<ComboItemEntity> itemsToDelete = new ArrayList<>();
        List<ComboItemEntity> itemsToAdd = new ArrayList<>();

        // Update existing quantities or add new products
        for (ComboItemRequestDto item : request.getItems()) {

            String productId = item.getProductId().trim();

            ComboItemEntity existingItem = existingMap.get(productId);

            if (existingItem != null) {
                // Existing product: update quantity
                existingItem.setQuantity(item.getQuantity());
            } else {
                // New product: insert new combo item
                ComboItemEntity newItem = new ComboItemEntity();
                newItem.setMasterId(masterId);
                newItem.setComboId(comboId);
                newItem.setProductId(productId);
                newItem.setQuantity(item.getQuantity());

                itemsToAdd.add(newItem);
            }
        }

        // Identify products removed from the request
        for (ComboItemEntity existingItem : existingItems) {

            if (!requestedProductIds.contains(
                    existingItem.getProductId())) {
                itemsToDelete.add(existingItem);
            }
        }

        // Delete removed items first
        comboItemRepository.deleteAll(itemsToDelete);

        // Flush deletions before inserting, avoiding unique-key conflicts
        comboItemRepository.flush();

        // Save new products
        comboItemRepository.saveAll(itemsToAdd);

        // Existing quantity changes are persisted by JPA
        return mapToResponse(combo, masterId);
    }



    // Soft delete: only change combo status
    @Transactional
    public String deactivateCombo(
            String comboId,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);

        ComboEntity combo = comboRepository
                .findByComboIdAndMasterId(
                        comboId, school.getMasterId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Combo not found"
                        ));

        if (combo.getStatus() == null ||
                combo.getStatus() != 1) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Combo is already inactive"
            );
        }

        combo.setStatus(0);
        comboRepository.save(combo);

        // Combo items and product stock remain unchanged
        return "Combo deactivated successfully";
    }

    // Validate combo request
    private void validateRequest(ComboRequestDto request) {

        if (request == null ||
                request.getComboName() == null ||
                request.getComboName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Combo name is required"
            );
        }

        if (request.getComboName().trim().length() > 150) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Combo name cannot exceed 150 characters"
            );
        }

        if (request.getItems() == null ||
                request.getItems().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one product is required"
            );
        }
    }

    // Validate logged-in school
    private UserEntity getActiveSchool(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        ));

        if (user.getRole() == null ||
                user.getRole() != 1) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only school accounts can manage combos"
            );
        }

        if (user.getStatus() == null ||
                user.getStatus() != 1) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "School account is inactive"
            );
        }

        return user;
    }

    // Convert entity to response DTO
    private ComboResponseDto mapToResponse(
            ComboEntity combo,
            String masterId) {

        List<ComboItemEntity> items =
                comboItemRepository
                        .findByMasterIdAndComboIdOrderByIdAsc(
                                masterId, combo.getComboId());

        List<ComboItemResponseDto> itemResponses =
                items.stream()
                        .map(item -> {
                            ProductEntity product =
                                    productRepository
                                            .findByProductIdAndMasterId(
                                                    item.getProductId(),
                                                    masterId)
                                            .orElseThrow(() ->
                                                    new ResponseStatusException(
                                                            HttpStatus.NOT_FOUND,
                                                            "Combo product not found"
                                                    ));

                            return ComboItemResponseDto.builder()
                                    .productId(product.getProductId())
                                    .productName(product.getProductName())
                                    .categoryId(product.getCategoryId())
                                    .uom(product.getUom())
                                    .productImageLink(
                                            product.getProductImageLink())
                                    .quantity(item.getQuantity())
                                    .build();
                        })
                        .toList();

        return ComboResponseDto.builder()
                .comboId(combo.getComboId())
                .comboName(combo.getComboName())
                .status(combo.getStatus())
                .createdAt(combo.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
