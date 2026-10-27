
package com.school.inventary.controller;

import com.school.inventary.dto.StockManagementResponseDto;
import com.school.inventary.service.StockManagementService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school/stock-management")
@RequiredArgsConstructor
public class StockManagementController {

    private final StockManagementService stockManagementService;

    @GetMapping("/get-all")
    public ResponseEntity<List<StockManagementResponseDto>> getCurrentStock(
            Authentication authentication) {

        List<StockManagementResponseDto> response =
                stockManagementService.getCurrentStock(
                        authentication.getName());

        return ResponseEntity.ok(response);
    }
}
