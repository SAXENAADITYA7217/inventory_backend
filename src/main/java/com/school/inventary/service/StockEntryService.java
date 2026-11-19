package com.school.inventary.service;

import com.school.inventary.dto.*;
import com.school.inventary.entity.*;
import com.school.inventary.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockEntryService {

    private final StockEntryRepository stockEntryRepository;
    private final StockEntryDataRepository stockEntryDataRepository;
    private final StockManagementRepository stockManagementRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public StockEntryResponseDto createStockEntry(StockEntryRequestDto request, String loggedInEmail) {
        // Validate logged-in school
        UserEntity school = userRepository.findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Active User Not Found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only school accounts can create stock entries"
            );
        }

        String masterId = school.getMasterId();

        // Validate invoice date cannot be in the future
        if (request.getInvoiceDate() != null && request.getInvoiceDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invoice date cannot be in the future"
            );
        }

        // Validate vendor belongs to this school and is active
        VendorEntity vendor = vendorRepository.findByMasterIdAndVendorIdAndStatus(masterId, request.getVendorId().trim(), 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active vendor not found for this school"));

        String trimmedInvoiceNo = request.getInvoiceNo().trim();

        // Prevent duplicate invoice numbers for the same vendor in this school
        if (stockEntryRepository.existsByMasterIdAndVendorIdAndInvoiceNoIgnoreCaseAndStatus(
                masterId, vendor.getVendorId(), trimmedInvoiceNo, 1)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active stock entry with invoice #" + trimmedInvoiceNo + " already exists for this vendor"
            );
        }

        // Validate products before saving anything
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one product is required");
        }

        List<ProductEntity> products = new ArrayList<>();
        Set<String> processedProductIds = new HashSet<>();

        for (StockEntryItemRequestDto item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
            }

            String pId = item.getProductId().trim();
            if (!processedProductIds.add(pId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate product in stock entry: " + pId
                );
            }

            ProductEntity product = productRepository.findByProductIdAndMasterId(pId, masterId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + pId));

            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactive product: " + pId);
            }
            if (item.getRate() == null || item.getRate().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rate cannot be negative");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
            }

            products.add(product);
        }

        // Upload PDF if provided
        String documentLink = uploadDocument(request.getAttachDocument());

        // Create stock entry header
        StockEntryEntity entry = StockEntryEntity.builder()
                .masterId(masterId)
                .vendorId(vendor.getVendorId())
                .invoiceNo(trimmedInvoiceNo)
                .invoiceDate(request.getInvoiceDate())
                .receivedPerson(request.getReceivedPerson().trim())
                .otherNote(request.getOtherNote() == null ? "" : request.getOtherNote().trim())
                .attachDocumentLink(documentLink)
                .status(1)
                .stockDate(LocalDate.now())
                .build();

        entry = stockEntryRepository.save(entry);

        // Save each product row and update current stock
        List<StockEntryItemResponseDto> itemResponse = new ArrayList<>();

        for (int i = 0; i < request.getItems().size(); i++) {
            StockEntryItemRequestDto item = request.getItems().get(i);
            ProductEntity product = products.get(i);

            StockEntryDataEntity entryData = StockEntryDataEntity.builder()
                    .masterId(masterId)
                    .stockId(entry.getStockId())
                    .productId(product.getProductId())
                    .rate(item.getRate())
                    .quantity(item.getQuantity())
                    .status(1)
                    .build();

            stockEntryDataRepository.save(entryData);

            // Find existing stock record for this school and product
            StockManagementEntity stock = stockManagementRepository.findByMasterIdAndProductId(masterId, product.getProductId())
                    .orElseGet(() -> StockManagementEntity.builder()
                            .masterId(masterId)
                            .productId(product.getProductId())
                            .quantity(BigDecimal.ZERO)
                            .openingStock(BigDecimal.ZERO)
                            .build());

            // Add received quantity to current stock
            stock.setQuantity(stock.getQuantity().add(item.getQuantity()));
            stockManagementRepository.save(stock);

            itemResponse.add(StockEntryItemResponseDto.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .uom(product.getUom())
                    .description(product.getProductDescription())
                    .rate(item.getRate())
                    .quantity(item.getQuantity())
                    .build());
        }

        return StockEntryResponseDto.builder()
                .stockId(entry.getStockId())
                .vendorId(entry.getVendorId())
                .vendorName(vendor.getVendorName())
                .invoiceNo(entry.getInvoiceNo())
                .invoiceDate(entry.getInvoiceDate())
                .receivedPerson(entry.getReceivedPerson())
                .otherNote(entry.getOtherNote())
                .attachDocumentLink(entry.getAttachDocumentLink())
                .status(entry.getStatus())
                .stockDate(entry.getStockDate())
                .createdAt(entry.getCreatedAt())
                .items(itemResponse)
                .build();
    }

    private String uploadDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must be under 10MB");
        }

        try {
            Path uploadDir = Paths.get("uploads", "stock-entry");
            Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID().toString() + ".pdf";
            Path target = uploadDir.resolve(fileName).normalize();

            Files.copy(file.getInputStream(), target);
            return fileName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }
    }

    @Transactional
    public StockEntryResponseDto getStockEntryById(String stockId, String loggedInEmail) {
        UserEntity school = userRepository.findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only school users can view stock entries");
        }

        String masterId = school.getMasterId();

        StockEntryEntity entry = stockEntryRepository.findByMasterIdAndStockId(masterId, stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock entry not found"));

        VendorEntity vendor = vendorRepository.findByMasterIdAndVendorId(masterId, entry.getVendorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        List<StockEntryDataEntity> items = stockEntryDataRepository.findByMasterIdAndStockIdOrderByIdAsc(masterId, entry.getStockId());

        List<StockEntryItemResponseDto> itemResponses = items.stream()
                .map(item -> {
                    ProductEntity product = productRepository.findByProductIdAndMasterId(item.getProductId(), masterId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + item.getProductId()));

                    return StockEntryItemResponseDto.builder()
                            .productId(product.getProductId())
                            .productName(product.getProductName())
                            .uom(product.getUom())
                            .description(product.getProductDescription())
                            .rate(item.getRate())
                            .quantity(item.getQuantity())
                            .build();
                })
                .toList();

        return StockEntryResponseDto.builder()
                .stockId(entry.getStockId())
                .vendorId(entry.getVendorId())
                .vendorName(vendor.getVendorName())
                .invoiceNo(entry.getInvoiceNo())
                .invoiceDate(entry.getInvoiceDate())
                .receivedPerson(entry.getReceivedPerson())
                .otherNote(entry.getOtherNote())
                .attachDocumentLink(entry.getAttachDocumentLink())
                .status(entry.getStatus())
                .stockDate(entry.getStockDate())
                .createdAt(entry.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    @Transactional
    public org.springframework.core.io.Resource getStockEntryDocument(String stockId, String loggedInEmail) {
        UserEntity school = userRepository.findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only school accounts can view stock documents");
        }

        StockEntryEntity entry = stockEntryRepository.findByMasterIdAndStockId(school.getMasterId(), stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock entry not found"));

        String documentLink = entry.getAttachDocumentLink();
        if (documentLink == null || documentLink.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No document attached to this stock entry");
        }

        try {
            Path uploadRoot = Paths.get("uploads", "stock-entry").toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(documentLink).normalize();

            if (!filePath.startsWith(uploadRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File does not exist on disk");
            }

            return new org.springframework.core.io.UrlResource(filePath.toUri());
        } catch (InvalidPathException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file: " + e.getMessage());
        }
    }

    @Transactional
    public List<StockEntryResponseDto> getAllStockEntries(String loggedInEmail) {
        UserEntity school = userRepository.findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only school accounts can view stock entries");
        }

        String masterId = school.getMasterId();

        List<StockEntryEntity> entries = stockEntryRepository.findByMasterIdOrderByCreatedAtDesc(masterId);

        return entries.stream()
                .map(entry -> {
                    VendorEntity vendor = vendorRepository.findByMasterIdAndVendorId(masterId, entry.getVendorId()).orElse(null);

                    List<StockEntryDataEntity> items = stockEntryDataRepository.findByMasterIdAndStockIdOrderByIdAsc(masterId, entry.getStockId());

                    List<StockEntryItemResponseDto> itemResponses = items.stream()
                            .map(item -> {
                                ProductEntity product = productRepository.findByProductIdAndMasterId(item.getProductId(), masterId).orElse(null);

                                return StockEntryItemResponseDto.builder()
                                        .productId(item.getProductId())
                                        .productName(product != null ? product.getProductName() : null)
                                        .uom(product != null ? product.getUom() : null)
                                        .description(product != null ? product.getProductDescription() : null)
                                        .rate(item.getRate())
                                        .quantity(item.getQuantity())
                                        .build();
                            })
                            .toList();

                    return StockEntryResponseDto.builder()
                            .stockId(entry.getStockId())
                            .vendorId(entry.getVendorId())
                            .vendorName(vendor != null ? vendor.getVendorName() : null)
                            .invoiceNo(entry.getInvoiceNo())
                            .invoiceDate(entry.getInvoiceDate())
                            .receivedPerson(entry.getReceivedPerson())
                            .otherNote(entry.getOtherNote())
                            .attachDocumentLink(entry.getAttachDocumentLink())
                            .status(entry.getStatus())
                            .stockDate(entry.getStockDate())
                            .createdAt(entry.getCreatedAt())
                            .items(itemResponses)
                            .build();
                })
                .toList();
    }

    @Transactional
    public StockEntryResponseDto updateStockEntry(String stockId, StockEntryUpdateRequestDto request, String loggedInEmail) {
        UserEntity school = userRepository.findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only school users can edit stock entries");
        }

        String masterId = school.getMasterId();

        StockEntryEntity entry = stockEntryRepository.findByMasterIdAndStockId(masterId, stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock entry not found"));

        if (entry.getStatus() == null || entry.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active stock entries can be edited");
        }

        if (request.getInvoiceDate() != null && request.getInvoiceDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice date cannot be in the future");
        }

        VendorEntity vendor = vendorRepository.findByMasterIdAndVendorIdAndStatus(masterId, request.getVendorId().trim(), 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or inactive vendor"));

        entry.setVendorId(vendor.getVendorId());
        entry.setInvoiceNo(request.getInvoiceNo().trim());
        entry.setInvoiceDate(request.getInvoiceDate());
        entry.setReceivedPerson(request.getReceivedPerson().trim());
        entry.setOtherNote(request.getOtherNote() == null ? "" : request.getOtherNote().trim());

        stockEntryRepository.save(entry);

        // Add new products only if provided
        List<StockEntryItemRequestDto> items = request.getItems();
        if (items != null && !items.isEmpty()) {
            Set<String> newProductIds = new HashSet<>();

            for (StockEntryItemRequestDto item : items) {
                if (item.getProductId() == null || item.getProductId().trim().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
                }

                String cleanProductId = item.getProductId().trim();

                if (!newProductIds.add(cleanProductId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate product in request: " + cleanProductId);
                }

                if (stockEntryDataRepository.existsByMasterIdAndStockIdAndProductId(masterId, stockId, cleanProductId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product already exists in this invoice: " + cleanProductId);
                }

                ProductEntity product = productRepository.findByProductIdAndMasterId(cleanProductId, masterId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + cleanProductId));

                if (product.getStatus() == null || product.getStatus() != 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is inactive: " + cleanProductId);
                }

                if (item.getRate() == null || item.getRate().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rate cannot be negative");
                }

                if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
                }

                StockEntryDataEntity entryData = StockEntryDataEntity.builder()
                        .masterId(masterId)
                        .stockId(entry.getStockId())
                        .productId(cleanProductId)
                        .rate(item.getRate())
                        .quantity(item.getQuantity())
                        .status(1)
                        .build();

                stockEntryDataRepository.save(entryData);

                StockManagementEntity stock = stockManagementRepository.findByMasterIdAndProductId(masterId, cleanProductId)
                        .orElseGet(() -> StockManagementEntity.builder()
                                .masterId(masterId)
                                .productId(cleanProductId)
                                .quantity(BigDecimal.ZERO)
                                .openingStock(BigDecimal.ZERO)
                                .build());

                stock.setQuantity(stock.getQuantity().add(item.getQuantity()));
                stockManagementRepository.save(stock);
            }
        }

        return getStockEntryById(stockId, loggedInEmail);
    }
}
