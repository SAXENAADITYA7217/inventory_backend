
package com.school.inventary.service;

import com.school.inventary.dto.VendorRequestDto;
import com.school.inventary.dto.VendorResponseDto;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.entity.VendorEntity;
import com.school.inventary.repo.UserRepository;
import com.school.inventary.repo.VendorRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    // Add vendor
    @Transactional
    public VendorResponseDto addVendor(
            VendorRequestDto request,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);

        String masterId = school.getMasterId();

        String vendorName = request.getVendorName().trim();
        String phoneNo = request.getPhoneNo().trim();

        String gstNo = request.getGstNo() == null
                ? null
                : request.getGstNo().trim().toUpperCase(Locale.ROOT);

        if (gstNo != null && gstNo.isBlank()) {
            gstNo = null;
        }

        // Check duplicate GST only when provided
        if (gstNo != null &&
                vendorRepository
                        .existsByMasterIdAndGstNoIgnoreCaseAndStatus(
                                masterId, gstNo, 1)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This GST number already exists for your school"
            );
        }

        VendorEntity vendor = VendorEntity.builder()
                .masterId(masterId)
                .vendorName(vendorName)
                .phoneNo(phoneNo)
                .gstNo(gstNo)
                .address(
                        request.getAddress() == null
                                ? null
                                : request.getAddress().trim()
                )
                .status(request.getStatus() == null ? 1 : request.getStatus())
                .build();

        VendorEntity savedVendor = vendorRepository.save(vendor);

        return mapToResponse(savedVendor);
    }

    // Get all active vendors of logged-in school
    @Transactional(readOnly = true)
    public List<VendorResponseDto> getAllVendors(String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);

        return vendorRepository
                .findByMasterIdOrderByCreatedAtDesc(school.getMasterId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Validate logged-in school account
    private UserEntity getActiveSchool(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        ));

        if (user.getRole() == null || user.getRole() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only school accounts can manage vendors"
            );
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "School account is inactive"
            );
        }

        return user;
    }


    @Transactional
    public VendorResponseDto updateVendor(
            String vendorId,
            VendorRequestDto request,
            String loggedInEmail) {

        UserEntity school = getActiveSchool(loggedInEmail);
        String masterId = school.getMasterId();

        if (request == null ||
                request.getVendorName() == null ||
                request.getVendorName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vendor name is required"
            );
        }

        VendorEntity vendor = vendorRepository
                .findByMasterIdAndVendorIdAndStatus(
                        masterId, vendorId, 1)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Active vendor not found"
                        ));

        String vendorName = request.getVendorName().trim();
        String phoneNo = request.getPhoneNo() == null
                ? null
                : request.getPhoneNo().trim();

        String gstNo = request.getGstNo() == null
                ? null
                : request.getGstNo().trim().toUpperCase(
                java.util.Locale.ROOT);

        if (gstNo != null && gstNo.isBlank()) {
            gstNo = null;
        }

        if (phoneNo == null || !phoneNo.matches("^[6-9][0-9]{9}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a valid 10-digit Indian mobile number"
            );
        }

        if (gstNo != null &&
                !gstNo.matches("(?i)^[0-9A-Z]{15}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "GST number must contain 15 alphanumeric characters"
            );
        }

        if (gstNo != null &&
                vendorRepository
                        .existsByMasterIdAndGstNoIgnoreCaseAndStatusAndVendorIdNot(
                                masterId, gstNo, 1, vendorId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This GST number already exists for another vendor"
            );
        }

        vendor.setVendorName(vendorName);
        vendor.setPhoneNo(phoneNo);
        vendor.setGstNo(gstNo);
        vendor.setAddress(
                request.getAddress() == null
                        ? null
                        : request.getAddress().trim()
        );
        vendor.setStatus(
                request.getStatus() == null ? 1 : request.getStatus()
        );

        VendorEntity updatedVendor = vendorRepository.save(vendor);

        return mapToResponse(updatedVendor);
    }


    // Entity to response DTO
    private VendorResponseDto mapToResponse(VendorEntity vendor) {

        return VendorResponseDto.builder()
                .vendorId(vendor.getVendorId())
                .vendorName(vendor.getVendorName())
                .phoneNo(vendor.getPhoneNo())
                .gstNo(vendor.getGstNo())
                .address(vendor.getAddress())
                .status(vendor.getStatus())
                .createdAt(vendor.getCreatedAt())
                .build();
    }
}
