
package com.school.inventary.controller;

import com.school.inventary.dto.StockEntryRequestDto;
import com.school.inventary.dto.StockEntryResponseDto;
import com.school.inventary.service.StockEntryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/school/stock-entries")
@RequiredArgsConstructor
@Validated
public class StockEntryController {

    private final StockEntryService stockEntryService;

    @PostMapping(
            value = "/add",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<StockEntryResponseDto> createStockEntry(
            @Valid @ModelAttribute StockEntryRequestDto request,
            Authentication authentication) {

        StockEntryResponseDto response =
                stockEntryService.createStockEntry(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{stockId}")
    public ResponseEntity<StockEntryResponseDto> getStockEntryById(
            @PathVariable String stockId,
            Authentication authentication) {

        StockEntryResponseDto response =
                stockEntryService.getStockEntryById(
                        stockId,
                        authentication.getName());

        return ResponseEntity.ok(response);
    }


    @GetMapping(
            value = "/{stockId}/document",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<org.springframework.core.io.Resource> viewStockEntryDocument(
            @PathVariable String stockId,
            Authentication authentication) {

        org.springframework.core.io.Resource pdf =
                stockEntryService.getStockEntryDocument(
                        stockId,
                        authentication.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"invoice.pdf\""
                )
                .body(pdf);
    }



    @GetMapping("/get-all")
    public ResponseEntity<java.util.List<StockEntryResponseDto>> getAllStockEntries(
            Authentication authentication) {

        java.util.List<StockEntryResponseDto> response =
                stockEntryService.getAllStockEntries(
                        authentication.getName());

        return ResponseEntity.ok(response);
    }
}