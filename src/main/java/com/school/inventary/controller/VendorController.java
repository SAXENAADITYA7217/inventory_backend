
package com.school.inventary.controller;

import com.school.inventary.dto.VendorRequestDto;
import com.school.inventary.dto.VendorResponseDto;
import com.school.inventary.service.VendorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    // Add vendor
    @PostMapping("/add")
    public ResponseEntity<VendorResponseDto> addVendor(
            @Valid @RequestBody VendorRequestDto request,
            Authentication authentication) {

        VendorResponseDto response = vendorService.addVendor(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }



    @PutMapping("/update/{vendorId}")
    public ResponseEntity<VendorResponseDto> updateVendor(
            @PathVariable String vendorId,
            @Valid @RequestBody VendorRequestDto request,
            Authentication authentication) {

        VendorResponseDto response = vendorService.updateVendor(
                vendorId,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }


    // Get all active vendors
    @GetMapping("/get-all")
    public ResponseEntity<List<VendorResponseDto>> getAllVendors(
            Authentication authentication) {

        return ResponseEntity.ok(
                vendorService.getAllVendors(authentication.getName())
        );
    }
}
