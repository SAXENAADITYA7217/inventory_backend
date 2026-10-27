
package com.school.inventary.service;

import com.school.inventary.dto.StockManagementResponseDto;
import com.school.inventary.entity.ProductEntity;
import com.school.inventary.entity.StockManagementEntity;
import com.school.inventary.entity.UserEntity;

import com.school.inventary.repo.ProductRepository;
import com.school.inventary.repo.StockManagementRepository;
import com.school.inventary.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockManagementService {

    private final StockManagementRepository stockManagementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<StockManagementResponseDto> getCurrentStock(
            String loggedInEmail) {

        UserEntity school = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Active user not found"));

        if (school.getRole() == null || school.getRole() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only school users can view current stock");
        }

        String masterId = school.getMasterId();

        List<StockManagementEntity> stockRecords =
                stockManagementRepository.findByMasterId(masterId);

        return stockRecords.stream()
                .map(stock -> {

                    ProductEntity product = productRepository
                            .findByProductIdAndMasterId(
                                    stock.getProductId(), masterId)
                            .orElse(null);

                    return StockManagementResponseDto.builder()
                            .productId(stock.getProductId())
                            .productName(product != null
                                    ? product.getProductName() : null)
                            .uom(product != null
                                    ? product.getUom() : null)
                            .description(product != null
                                    ? product.getProductDescription() : null)
                            .openingStock(stock.getOpeningStock())
                            .currentStock(stock.getQuantity())
                            .lastUpdateDate(stock.getLastUpdateDate())
                            .build();
                })
                .toList();
    }
}
