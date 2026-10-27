
package com.school.inventary.controller;

import com.school.inventary.dto.ComboRequestDto;
import com.school.inventary.dto.ComboResponseDto;
import com.school.inventary.service.ComboService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    // Create combo
    @PostMapping("/add")
    public ResponseEntity<ComboResponseDto> createCombo(
            @Valid @RequestBody ComboRequestDto request,
            Authentication authentication) {

        ComboResponseDto response = comboService.createCombo(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all active combos
    @GetMapping("/get-all-combo")
    public ResponseEntity<List<ComboResponseDto>> getAllCombos(
            Authentication authentication) {

        return ResponseEntity.ok(
                comboService.getAllCombos(authentication.getName())
        );
    }

    // Get combo by ID
    @GetMapping("/{comboId}")
    public ResponseEntity<ComboResponseDto> getComboById(
            @PathVariable String comboId,
            Authentication authentication) {

        return ResponseEntity.ok(
                comboService.getComboById(
                        comboId,
                        authentication.getName()
                )
        );
    }


    // Update combo
    @PutMapping("/{comboId}")
    public ResponseEntity<ComboResponseDto> updateCombo(
            @PathVariable String comboId,
            @Valid @RequestBody ComboRequestDto request,
            Authentication authentication) {

        ComboResponseDto response = comboService.updateCombo(
                comboId,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }


    // Deactivate combo
    @DeleteMapping("/{comboId}")
    public ResponseEntity<String> deactivateCombo(
            @PathVariable String comboId,
            Authentication authentication) {

        return ResponseEntity.ok(
                comboService.deactivateCombo(
                        comboId,
                        authentication.getName()
                )
        );
    }
}
