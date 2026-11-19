package com.school.inventary;

import com.school.inventary.dto.StockEntryItemRequestDto;
import com.school.inventary.dto.StockEntryRequestDto;
import com.school.inventary.dto.StockEntryResponseDto;
import com.school.inventary.dto.StockEntryUpdateRequestDto;
import com.school.inventary.entity.*;
import com.school.inventary.repo.*;
import com.school.inventary.service.StockEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockEntryEditWorkflowTest {

    @Autowired
    private StockEntryService stockEntryService;

    @Autowired
    private StockEntryRepository stockEntryRepository;

    @Autowired
    private StockEntryDataRepository stockEntryDataRepository;

    @Autowired
    private StockManagementRepository stockManagementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductRepository productRepository;

    private String schoolEmail;
    private String masterId;
    private VendorEntity vendor1;
    private VendorEntity vendor2;
    private ProductEntity product1;
    private ProductEntity product2;
    private ProductEntity product3;

    @BeforeEach
    void setup() {
        masterId = "MST_" + UUID.randomUUID().toString().substring(0, 8);
        schoolEmail = "school_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";

        // Create active school user
        UserEntity school = new UserEntity();
        school.setEmail(schoolEmail);
        school.setMasterId(masterId);
        school.setName("Test School");
        school.setSchoolName("Test Academy");
        school.setPhoneNumber("9888888888");
        school.setPassword("password123");
        school.setRole(1); // 1 = SCHOOL
        school.setStatus(1); // 1 = ACTIVE
        userRepository.save(school);

        // Create vendors
        vendor1 = vendorRepository.save(VendorEntity.builder()
                .masterId(masterId)
                .vendorId("VEN_" + UUID.randomUUID().toString().substring(0, 8))
                .vendorName("Vendor One")
                .gstNo("29AAAAA0000A1Z5")
                .phoneNo("9111111111")
                .status(1)
                .build());

        vendor2 = vendorRepository.save(VendorEntity.builder()
                .masterId(masterId)
                .vendorId("VEN_" + UUID.randomUUID().toString().substring(0, 8))
                .vendorName("Vendor Two")
                .gstNo("29BBBBB0000B1Z5")
                .phoneNo("9222222222")
                .status(1)
                .build());

        // Create products
        ProductEntity p1 = new ProductEntity();
        p1.setMasterId(masterId);
        p1.setProductId("PRD_" + UUID.randomUUID().toString().substring(0, 8));
        p1.setProductName("Notebooks");
        p1.setCategoryId("CAT_STATIONERY");
        p1.setUom("PCS");
        p1.setStatus(1);
        product1 = productRepository.save(p1);

        ProductEntity p2 = new ProductEntity();
        p2.setMasterId(masterId);
        p2.setProductId("PRD_" + UUID.randomUUID().toString().substring(0, 8));
        p2.setProductName("Pens");
        p2.setCategoryId("CAT_STATIONERY");
        p2.setUom("PCS");
        p2.setStatus(1);
        product2 = productRepository.save(p2);

        ProductEntity p3 = new ProductEntity();
        p3.setMasterId(masterId);
        p3.setProductId("PRD_" + UUID.randomUUID().toString().substring(0, 8));
        p3.setProductName("Markers");
        p3.setCategoryId("CAT_STATIONERY");
        p3.setUom("PCS");
        p3.setStatus(1);
        product3 = productRepository.save(p3);
    }

    @Test
    void testHeaderOnlyUpdate_PreservesItemsAndInventoryAndPdf() {
        // 1. Create initial Stock Entry with product1
        StockEntryRequestDto createReq = new StockEntryRequestDto();
        createReq.setVendorId(vendor1.getVendorId());
        createReq.setInvoiceNo("INV-001");
        createReq.setInvoiceDate(LocalDate.of(2026, 10, 20));
        createReq.setReceivedPerson("Alice");
        createReq.setOtherNote("Initial entry");

        StockEntryItemRequestDto item1 = StockEntryItemRequestDto.builder()
                .productId(product1.getProductId())
                .rate(new BigDecimal("50.00"))
                .quantity(new BigDecimal("10.000"))
                .build();
        createReq.setItems(List.of(item1));

        StockEntryResponseDto created = stockEntryService.createStockEntry(createReq, schoolEmail);
        assertNotNull(created.getStockId());
        assertEquals(1, created.getItems().size());

        // Verify initial inventory
        StockManagementEntity stock1Before = stockManagementRepository
                .findByMasterIdAndProductId(masterId, product1.getProductId())
                .orElseThrow();
        assertEquals(0, new BigDecimal("10.000").compareTo(stock1Before.getQuantity()));

        // 2. Perform header-only update
        StockEntryUpdateRequestDto updateReq = StockEntryUpdateRequestDto.builder()
                .vendorId(vendor2.getVendorId())
                .invoiceNo("INV-001-MOD")
                .invoiceDate(LocalDate.of(2026, 10, 25))
                .receivedPerson("Alice Smith")
                .otherNote("Updated header note")
                .items(null) // Header-only (no items)
                .build();

        StockEntryResponseDto updated = stockEntryService.updateStockEntry(created.getStockId(), updateReq, schoolEmail);

        // Verify header fields updated
        assertEquals(created.getStockId(), updated.getStockId()); // StockId unchanged
        assertEquals(vendor2.getVendorId(), updated.getVendorId());
        assertEquals("Vendor Two", updated.getVendorName());
        assertEquals("INV-001-MOD", updated.getInvoiceNo());
        assertEquals(LocalDate.of(2026, 10, 25), updated.getInvoiceDate());
        assertEquals("Alice Smith", updated.getReceivedPerson());
        assertEquals("Updated header note", updated.getOtherNote());

        // Verify existing item remained unchanged
        assertEquals(1, updated.getItems().size());
        assertEquals(product1.getProductId(), updated.getItems().get(0).getProductId());
        assertEquals(0, new BigDecimal("50.00").compareTo(updated.getItems().get(0).getRate()));
        assertEquals(0, new BigDecimal("10.000").compareTo(updated.getItems().get(0).getQuantity()));

        // Verify inventory of product1 did NOT change
        StockManagementEntity stock1After = stockManagementRepository
                .findByMasterIdAndProductId(masterId, product1.getProductId())
                .orElseThrow();
        assertEquals(0, new BigDecimal("10.000").compareTo(stock1After.getQuantity()));
    }

    @Test
    void testAddingNewProduct_IncrementsOnlyNewInventory() {
        // 1. Create initial Stock Entry with product1 (qty 15)
        StockEntryRequestDto createReq = new StockEntryRequestDto();
        createReq.setVendorId(vendor1.getVendorId());
        createReq.setInvoiceNo("INV-002");
        createReq.setInvoiceDate(LocalDate.of(2026, 10, 22));
        createReq.setReceivedPerson("Bob");
        createReq.setOtherNote("First shipment");

        StockEntryItemRequestDto item1 = StockEntryItemRequestDto.builder()
                .productId(product1.getProductId())
                .rate(new BigDecimal("40.00"))
                .quantity(new BigDecimal("15.000"))
                .build();
        createReq.setItems(List.of(item1));

        StockEntryResponseDto created = stockEntryService.createStockEntry(createReq, schoolEmail);

        // Verify inventory for product1 = 15, product2 does not exist yet
        StockManagementEntity stock1 = stockManagementRepository
                .findByMasterIdAndProductId(masterId, product1.getProductId())
                .orElseThrow();
        assertEquals(0, new BigDecimal("15.000").compareTo(stock1.getQuantity()));
        assertTrue(stockManagementRepository.findByMasterIdAndProductId(masterId, product2.getProductId()).isEmpty());

        // 2. Edit stock entry and add missing product2 (qty 20)
        StockEntryUpdateRequestDto updateReq = StockEntryUpdateRequestDto.builder()
                .vendorId(vendor1.getVendorId())
                .invoiceNo("INV-002")
                .invoiceDate(LocalDate.of(2026, 10, 22))
                .receivedPerson("Bob")
                .otherNote("Added missing pens")
                .items(List.of(
                        StockEntryItemRequestDto.builder()
                                .productId(product2.getProductId())
                                .rate(new BigDecimal("12.50"))
                                .quantity(new BigDecimal("20.000"))
                                .build()
                ))
                .build();

        StockEntryResponseDto updated = stockEntryService.updateStockEntry(created.getStockId(), updateReq, schoolEmail);

        // Verify stock entry now has 2 products
        assertEquals(2, updated.getItems().size());

        // product1 quantity in stock remains 15 (NOT doubled!)
        StockManagementEntity stock1After = stockManagementRepository
                .findByMasterIdAndProductId(masterId, product1.getProductId())
                .orElseThrow();
        assertEquals(0, new BigDecimal("15.000").compareTo(stock1After.getQuantity()));

        // product2 inventory is created and equals 20
        StockManagementEntity stock2After = stockManagementRepository
                .findByMasterIdAndProductId(masterId, product2.getProductId())
                .orElseThrow();
        assertEquals(0, new BigDecimal("20.000").compareTo(stock2After.getQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(stock2After.getOpeningStock()));
    }

    @Test
    void testRejection_WhenAddingDuplicateProductOrExistingProduct() {
        // 1. Create initial Stock Entry with product1
        StockEntryRequestDto createReq = new StockEntryRequestDto();
        createReq.setVendorId(vendor1.getVendorId());
        createReq.setInvoiceNo("INV-003");
        createReq.setInvoiceDate(LocalDate.of(2026, 10, 23));
        createReq.setReceivedPerson("Charlie");

        StockEntryItemRequestDto item1 = StockEntryItemRequestDto.builder()
                .productId(product1.getProductId())
                .rate(new BigDecimal("10.00"))
                .quantity(new BigDecimal("5.000"))
                .build();
        createReq.setItems(List.of(item1));

        StockEntryResponseDto created = stockEntryService.createStockEntry(createReq, schoolEmail);

        // Attempt A: Re-add product1 which is already in invoice
        StockEntryUpdateRequestDto dupReq1 = StockEntryUpdateRequestDto.builder()
                .vendorId(vendor1.getVendorId())
                .invoiceNo("INV-003")
                .invoiceDate(LocalDate.of(2026, 10, 23))
                .receivedPerson("Charlie")
                .items(List.of(
                        StockEntryItemRequestDto.builder()
                                .productId(product1.getProductId()) // Already exists!
                                .rate(new BigDecimal("10.00"))
                                .quantity(new BigDecimal("3.000"))
                                .build()
                ))
                .build();

        assertThrows(ResponseStatusException.class, () ->
                stockEntryService.updateStockEntry(created.getStockId(), dupReq1, schoolEmail));

        // Attempt B: Pass duplicate product2 twice in the same request
        StockEntryUpdateRequestDto dupReq2 = StockEntryUpdateRequestDto.builder()
                .vendorId(vendor1.getVendorId())
                .invoiceNo("INV-003")
                .invoiceDate(LocalDate.of(2026, 10, 23))
                .receivedPerson("Charlie")
                .items(List.of(
                        StockEntryItemRequestDto.builder()
                                .productId(product2.getProductId())
                                .rate(new BigDecimal("15.00"))
                                .quantity(new BigDecimal("2.000"))
                                .build(),
                        StockEntryItemRequestDto.builder()
                                .productId(product2.getProductId()) // Duplicate in same request!
                                .rate(new BigDecimal("15.00"))
                                .quantity(new BigDecimal("4.000"))
                                .build()
                ))
                .build();

        assertThrows(ResponseStatusException.class, () ->
                stockEntryService.updateStockEntry(created.getStockId(), dupReq2, schoolEmail));
    }
}
