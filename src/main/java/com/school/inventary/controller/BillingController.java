package com.school.inventary.controller;

import com.school.inventary.dto.BillingRequestDto;
import com.school.inventary.dto.BillingResponseDto;
import com.school.inventary.dto.PaymentRequestDto;
import com.school.inventary.dto.PaymentResponseDto;
import com.school.inventary.service.BillingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/create")
    public ResponseEntity<BillingResponseDto> createBilling(
            @Valid @RequestBody BillingRequestDto request,
            Authentication authentication) {

        BillingResponseDto response = billingService.createBilling(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<BillingResponseDto>> getAllBilling(
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getAllBilling(authentication.getName())
        );
    }

    @GetMapping("/{billingId}")
    public ResponseEntity<BillingResponseDto> getBillingById(
            @PathVariable String billingId,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getBillingById(
                        billingId,
                        authentication.getName()
                )
        );
    }

    // Receive pending payment
    @PostMapping("/{billingId}/payment")
    public ResponseEntity<PaymentResponseDto> receivePayment(
            @PathVariable String billingId,
            @Valid @RequestBody PaymentRequestDto request,
            Authentication authentication) {

        PaymentResponseDto response = billingService.receivePayment(
                billingId,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }


    // Get complete payment history
    @GetMapping("/{billingId}/payments")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistory(
            @PathVariable String billingId,
            Authentication authentication) {

        return ResponseEntity.ok(
                billingService.getPaymentHistory(
                        billingId,
                        authentication.getName()
                )
        );
    }
}