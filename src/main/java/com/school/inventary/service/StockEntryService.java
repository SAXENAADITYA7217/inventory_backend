package com.school.inventary.service;

import com.school.inventary.dto.StockEntryItemRequestDto;
import com.school.inventary.dto.StockEntryItemResponseDto;
import com.school.inventary.dto.StockEntryRequestDto;
import com.school.inventary.dto.StockEntryResponseDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
    public StockEntryResponseDto createStockEntry(StockEntryRequestDto request , String loggedInEmail){
        // validate logged-in in school
        UserEntity school = userRepository.findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Active User Not Found"));

        if(school.getRole()==null || school.getRole()!=1){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "only school accounts can create stock entries"
            );
        }

        String masterId = school.getMasterId();

        // Validate vendor belongs to this school and is active

        VendorEntity vendor = vendorRepository.findByMasterIdAndVendorIdAndStatus(masterId, request.getVendorId(),1)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active vendor not found for this school"));

        // validate products before saving anything

        if(request.getItems()==null || request.getItems().isEmpty()){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one product is required");
        }

        List<ProductEntity> products = new ArrayList<>();

        for(StockEntryItemRequestDto item : request.getItems()){
            ProductEntity product = productRepository.findByProductIdAndMasterId(item.getProductId(),masterId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"+item.getProductId()));

            if(product.getStatus()==null || product.getStatus()!=1){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inactive product"+ item.getProductId());

            }
            if(item.getRate()==null || item.getRate().compareTo(BigDecimal.ZERO)<0){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rate cannot be negative");
            }
            if(item.getQuantity()==null || item.getQuantity().compareTo(BigDecimal.ZERO)<=0){
                throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
            }

            products.add(product);
        }

        // upload pdf if provided

        String documentLink = uploadDocument(request.getAttachDocument());

        // create stock entry header
        StockEntryEntity  entry = StockEntryEntity.builder()
                .masterId(masterId)
                .vendorId(vendor.getVendorId())
                .invoiceNo(request.getInvoiceNo())
                .invoiceDate(request.getInvoiceDate())
                .receivedPerson(request.getReceivedPerson())
                .otherNote(request.getOtherNote()==null ? "": request.getOtherNote())
                .attachDocumentLink(documentLink)
                .status(1)
                .stockDate(LocalDate.now())
                .build();

        entry = stockEntryRepository.save(entry);

        // save each product row and update current stock

        List<StockEntryItemResponseDto>  itemResponse= new ArrayList<>();

        for(int i = 0; i<request.getItems().size(); i++){
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

            // find existing stock record for this school and product

            StockManagementEntity stock = stockManagementRepository.findByMasterIdAndProductId(masterId,product.getProductId())
                    .orElseGet(()->StockManagementEntity.builder()
                            .masterId(masterId)
                            .productId(product.getProductId())
                            .quantity(BigDecimal.ZERO)
                            .openingStock(BigDecimal.ZERO)
                            .build());

            // add Received quantity to current stock
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

        // Return Response

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

    private String uploadDocument(MultipartFile file){
        if(file==null || file.isEmpty()){
            return null;
        }
        String originalFileName = file.getOriginalFilename();

        if(originalFileName==null || !originalFileName.toLowerCase().endsWith(".pdf")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only pdf documents are allowed");
        }

        if(file.getSize()>10*1024*1024){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pdf size must not exceed 10 mb");
        }
        try {
            Path uploadDir = Paths.get("uploads","stock-entry").toAbsolutePath().normalize();

            Files.createDirectories(uploadDir);

            String filename = UUID.randomUUID()+".pdf";

            Path destination = uploadDir.resolve(filename).normalize();

            if(!destination.startsWith(uploadDir)){
                throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }
            file.transferTo(destination);

            return "uploads/stock-entry/"+filename;


        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload PDF document");
        }
    }



    @Transactional
    public StockEntryResponseDto getStockEntryById(
            String stockId,
            String loggedInEmail) {

        // 1. Validate logged-in school
        UserEntity school = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only school accounts can view stock entries");
        }

        String masterId = school.getMasterId();

        // 2. Find stock entry belonging to this school
        StockEntryEntity entry = stockEntryRepository
                .findByMasterIdAndStockId(masterId, stockId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stock entry not found"));

        // 3. Get vendor details
        VendorEntity vendor = vendorRepository
                .findByMasterIdAndVendorId(
                        masterId,
                        entry.getVendorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vendor not found"));

        // 4. Get all product rows
        List<StockEntryDataEntity> items =
                stockEntryDataRepository
                        .findByMasterIdAndStockIdOrderByIdAsc(
                                masterId,
                                entry.getStockId());

        List<StockEntryItemResponseDto> itemResponses =
                items.stream()
                        .map(item -> {
                            ProductEntity product = productRepository
                                    .findByProductIdAndMasterId(
                                            item.getProductId(),
                                            masterId)
                                    .orElseThrow(() ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    "Product not found: "
                                                            + item.getProductId()));

                            return StockEntryItemResponseDto.builder()
                                    .productId(product.getProductId())
                                    .productName(product.getProductName())
                                    .uom(product.getUom())
                                    .description(
                                            product.getProductDescription())
                                    .rate(item.getRate())
                                    .quantity(item.getQuantity())
                                    .build();
                        })
                        .toList();

        // 5. Return complete entry details
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
    public org.springframework.core.io.Resource getStockEntryDocument(
            String stockId,
            String loggedInEmail) {

        UserEntity school = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only school accounts can view stock documents");
        }

        StockEntryEntity entry = stockEntryRepository
                .findByMasterIdAndStockId(
                        school.getMasterId(),
                        stockId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stock entry not found"));

        String documentLink = entry.getAttachDocumentLink();

        if (documentLink == null || documentLink.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No document attached to this stock entry");
        }

        try {
            Path uploadRoot = Paths.get(
                    "uploads", "stock-entry").toAbsolutePath().normalize();

            Path pdfPath = uploadRoot
                    .resolve(Paths.get(documentLink).getFileName())
                    .normalize();

            if (!pdfPath.startsWith(uploadRoot)
                    || !Files.isRegularFile(pdfPath)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "PDF document not found");
            }

            return new org.springframework.core.io.FileSystemResource(pdfPath);

        } catch (InvalidPathException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid document path");
        }
    }






    @Transactional
    public List<StockEntryResponseDto> getAllStockEntries(String loggedInEmail) {
        UserEntity school = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only school accounts can view stock entries");
        }

        String masterId = school.getMasterId();

        List<StockEntryEntity> entries =
                stockEntryRepository.findByMasterIdOrderByCreatedAtDesc(masterId);

        return entries.stream()
                .map(entry -> {
                    VendorEntity vendor = vendorRepository
                            .findByMasterIdAndVendorId(masterId, entry.getVendorId())
                            .orElse(null);

                    List<StockEntryDataEntity> items =
                            stockEntryDataRepository
                                    .findByMasterIdAndStockIdOrderByIdAsc(
                                            masterId,
                                            entry.getStockId());

                    List<StockEntryItemResponseDto> itemResponses =
                            items.stream()
                                    .map(item -> {
                                        ProductEntity product = productRepository
                                                .findByProductIdAndMasterId(item.getProductId(), masterId)
                                                .orElse(null);

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
}