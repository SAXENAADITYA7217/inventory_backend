package com.school.inventary.service;

import com.school.inventary.dto.*;
import com.school.inventary.entity.*;
import com.school.inventary.repo.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingInfoRepository billingInfoRepository;
    private final BillingItemRepository billingItemRepository;
    private final ProductRepository productRepository;
    private final StockManagementRepository stockManagementRepository;
    private final UserRepository userRepository;
    private final BillingPaymentRepository billingPaymentRepository;

    @Transactional
    public BillingResponseDto createBilling(
            BillingRequestDto request,
            String loggedInEmail) {

        // 1. Verify logged-in school
        UserEntity user = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() ->
                        new RuntimeException("School account not found"));

        if (user.getRole() != 1) {
            throw new RuntimeException("Only school can create billing");
        }

        String masterId = user.getMasterId();

        if (request.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Invalid paid amount");
        }

        String customerPhone = request.getPhoneNo() != null ? request.getPhoneNo().trim() : null;
        if (customerPhone != null && !customerPhone.isBlank() && !customerPhone.matches("^[0-9]{10}$")) {
            throw new RuntimeException("Customer mobile number must contain exactly 10 digits without alphabets or special characters");
        }

        // 2. Generate billing ID
        String billingId = "BILL_" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        int totalItems = 0;

        List<BillingItemEntity> billingItems = new ArrayList<>();

        Set<String> processedProducts = new HashSet<>();

        // 3. Process each product
        for (BillingItemRequestDto item : request.getItems()) {

            if (!processedProducts.add(item.getProductId())) {
                throw new RuntimeException(
                        "Duplicate product found: " + item.getProductId());
            }

            if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Quantity must be greater than zero");
            }

            if (item.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Discount cannot be negative");
            }

            ProductEntity product = productRepository
                    .findByProductIdAndMasterId(
                            item.getProductId(), masterId)
                    .orElseThrow(() ->
                            new RuntimeException("Product not found"));

            if (product.getStatus() != 1) {
                throw new RuntimeException(
                        "Product is inactive: " + product.getProductName());
            }

            // 4. Check available stock
            StockManagementEntity stock = stockManagementRepository
                    .findByMasterIdAndProductId(
                            masterId, item.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException("Stock record not found"));

            if (stock.getQuantity().compareTo(item.getQuantity()) < 0) {
                throw new RuntimeException(
                        "Insufficient stock for " + product.getProductName());
            }

            // 5. Get current selling price: use custom selling price if provided, otherwise default to product catalog selling price
            BigDecimal sellingPrice = item.getSellingPrice();
            if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
                sellingPrice = product.getSellingPrice();
            }

            if (sellingPrice == null ||
                    sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Invalid product selling price");
            }

            BigDecimal itemSubtotal = sellingPrice.multiply(item.getQuantity());

            if (item.getDiscount().compareTo(itemSubtotal) > 0) {
                throw new RuntimeException(
                        "Discount cannot exceed product amount");
            }

            BigDecimal itemTotal = itemSubtotal.subtract(item.getDiscount());

            subtotal = subtotal.add(itemSubtotal);
            totalDiscount = totalDiscount.add(item.getDiscount());

            totalItems++;

            // 6. Create billing item snapshot
            BillingItemEntity billingItem = BillingItemEntity.builder()
                    .masterId(masterId)
                    .billingId(billingId)
                    .itemId("ITEM_" +
                            UUID.randomUUID().toString().substring(0, 8))
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .uom(product.getUom())
                    .quantity(item.getQuantity())
                    .sellingPrice(sellingPrice)
                    .discount(item.getDiscount())
                    .totalAmount(itemTotal)
                    .status(1)
                    .build();

            billingItems.add(billingItem);

            // 7. Deduct stock
            stock.setQuantity(
                    stock.getQuantity().subtract(item.getQuantity()));

            stockManagementRepository.save(stock);
        }

        // 8. Calculate final bill
        BigDecimal totalAmount = subtotal.subtract(totalDiscount);
        BigDecimal amountPaid = request.getAmountPaid();

        if (amountPaid.compareTo(totalAmount) > 0) {
            throw new RuntimeException(
                    "Paid amount cannot exceed total bill amount");
        }

        BigDecimal balance = totalAmount.subtract(amountPaid);

        String paymentStatus;

        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = "PAID";
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = "PARTIAL";
        } else {
            paymentStatus = "UNPAID";
        }

        // 9. Save billing information
        String studentName = request.getStudentName() != null && !request.getStudentName().isBlank()
                ? request.getStudentName().trim()
                : (request.getCustomerName() != null && !request.getCustomerName().isBlank() ? request.getCustomerName().trim() : null);

        String customerName = request.getCustomerName() != null && !request.getCustomerName().isBlank()
                ? request.getCustomerName().trim()
                : studentName;

        String fatherName = request.getFatherName() != null && !request.getFatherName().isBlank()
                ? request.getFatherName().trim()
                : null;

        String className = request.getClassName() != null && !request.getClassName().isBlank()
                ? request.getClassName().trim()
                : (request.getStudentClass() != null && !request.getStudentClass().isBlank() ? request.getStudentClass().trim() : null);

        BillingInfoEntity billingInfo = BillingInfoEntity.builder()
                .masterId(masterId)
                .billingId(billingId)
                .customerName(customerName)
                .studentName(studentName)
                .fatherName(fatherName)
                .className(className)
                .phoneNo(customerPhone)
                .otherNote(request.getOtherNote() != null ? request.getOtherNote().trim() : null)
                .subtotal(subtotal)
                .totalDiscount(totalDiscount)
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .balance(balance)
                .totalItems(totalItems)
                .billingDate(LocalDate.now())
                .billingTime(LocalTime.now())
                .paymentStatus(paymentStatus)
                .status(1)
                .build();

        billingInfoRepository.save(billingInfo);

        // 10. Save all billing items
        billingItemRepository.saveAll(billingItems);

        return mapToResponse(billingInfo, billingItems);
    }

    @Transactional(readOnly = true)
    public List<BillingResponseDto> getAllBilling(String loggedInEmail) {

        UserEntity user = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (user.getRole() != 1) {
            throw new RuntimeException("Only school can access billing");
        }

        String masterId = user.getMasterId();

        List<BillingInfoEntity> bills =
                billingInfoRepository.findByMasterIdOrderByCreatedAtDesc(masterId);

        return bills.stream()
                .map(bill -> {
                    List<BillingItemEntity> items =
                            billingItemRepository
                                    .findByMasterIdAndBillingIdOrderByIdAsc(
                                            masterId, bill.getBillingId());

                    return mapToResponse(bill, items);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BillingResponseDto getBillingById(
            String billingId,
            String loggedInEmail) {

        UserEntity user = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (user.getRole() != 1) {
            throw new RuntimeException("Only school can access billing");
        }

        String masterId = user.getMasterId();

        BillingInfoEntity bill = billingInfoRepository
                .findByMasterIdAndBillingId(masterId, billingId)
                .orElseThrow(() -> new RuntimeException("Billing not found"));

        List<BillingItemEntity> items =
                billingItemRepository
                        .findByMasterIdAndBillingIdOrderByIdAsc(
                                masterId, billingId);

        return mapToResponse(bill, items);
    }

    @Transactional
    public PaymentResponseDto receivePayment(
            String billingId,
            PaymentRequestDto request,
            String loggedInEmail) {

        // 1. Verify school
        UserEntity user = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() ->
                        new RuntimeException("School not found"));

        if (user.getRole() != 1) {
            throw new RuntimeException("Only school can receive payments");
        }

        String masterId = user.getMasterId();

        // 2. Find bill belonging to this school
        BillingInfoEntity bill = billingInfoRepository
                .findByMasterIdAndBillingId(masterId, billingId)
                .orElseThrow(() ->
                        new RuntimeException("Billing not found"));

        if (bill.getStatus() != 1) {
            throw new RuntimeException("This bill is cancelled");
        }

        // 3. Validate payment
        BigDecimal paymentAmount = request.getPaymentAmount();

        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment must be greater than zero");
        }

        BigDecimal currentBalance = bill.getBalance() != null ? bill.getBalance() : BigDecimal.ZERO;

        if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("This bill is already fully paid");
        }

        if (paymentAmount.compareTo(currentBalance) > 0) {
            BigDecimal diff = paymentAmount.subtract(currentBalance);
            if (diff.compareTo(new BigDecimal("0.50")) <= 0) {
                paymentAmount = currentBalance;
            } else {
                throw new RuntimeException("Payment cannot exceed remaining balance (" + currentBalance + ")");
            }
        }

        String paymentMethod = request.getPaymentMethod() != null
                ? request.getPaymentMethod().trim().toUpperCase()
                : "CASH";

        if (!Set.of("CASH", "UPI", "CARD", "BANK", "OTHER")
                .contains(paymentMethod)) {
            throw new RuntimeException("Invalid payment method");
        }

        // 4. Create payment history record
        String paymentId = "PAY_" +
                UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase();

        LocalDate paymentDate = LocalDate.now();
        LocalTime paymentTime = LocalTime.now();

        BillingPaymentEntity payment = BillingPaymentEntity.builder()
                .masterId(masterId)
                .billingId(billingId)
                .paymentId(paymentId)
                .paymentAmount(paymentAmount)
                .paymentMethod(paymentMethod)
                .transactionReference(request.getTransactionReference())
                .note(request.getNote())
                .paymentDate(paymentDate)
                .paymentTime(paymentTime)
                .status(1)
                .build();

        billingPaymentRepository.save(payment);

        // 5. Update bill payment summary
        BigDecimal currentPaid = bill.getAmountPaid() != null ? bill.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal totalBillAmount = bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal updatedPaidAmount = currentPaid.add(paymentAmount);
        if (updatedPaidAmount.compareTo(totalBillAmount) > 0) {
            updatedPaidAmount = totalBillAmount;
        }

        BigDecimal updatedBalance = totalBillAmount.subtract(updatedPaidAmount);
        if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            updatedBalance = BigDecimal.ZERO;
        }

        bill.setAmountPaid(updatedPaidAmount);
        bill.setBalance(updatedBalance);

        if (updatedBalance.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setPaymentStatus("PAID");
        } else {
            bill.setPaymentStatus("PARTIAL");
        }

        billingInfoRepository.save(bill);

        // 6. Return payment response with complete bill & balance details
        return PaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .billingId(payment.getBillingId())
                .paymentAmount(payment.getPaymentAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(payment.getTransactionReference())
                .note(payment.getNote())
                .paymentDate(paymentDate)
                .paymentTime(paymentTime)
                .totalAmount(bill.getTotalAmount())
                .amountPaid(bill.getAmountPaid())
                .balance(bill.getBalance())
                .paymentStatus(bill.getPaymentStatus())
                .customerName(bill.getCustomerName())
                .studentName(bill.getStudentName() != null ? bill.getStudentName() : bill.getCustomerName())
                .fatherName(bill.getFatherName())
                .className(bill.getClassName())
                .studentClass(bill.getClassName())
                .phoneNo(bill.getPhoneNo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentHistory(
            String billingId,
            String loggedInEmail) {

        UserEntity user = userRepository
                .findByEmailAndStatus(loggedInEmail, 1)
                .orElseThrow(() ->
                        new RuntimeException("School not found"));

        if (user.getRole() != 1) {
            throw new RuntimeException("Only school can access payments");
        }

        String masterId = user.getMasterId();

        // Verify bill ownership
        billingInfoRepository
                .findByMasterIdAndBillingId(masterId, billingId)
                .orElseThrow(() ->
                        new RuntimeException("Billing not found"));

        return billingPaymentRepository
                .findByMasterIdAndBillingIdAndStatusOrderByCreatedAtAsc(
                        masterId, billingId, 1)
                .stream()
                .map(payment -> PaymentResponseDto.builder()
                        .paymentId(payment.getPaymentId())
                        .billingId(payment.getBillingId())
                        .paymentAmount(payment.getPaymentAmount())
                        .paymentMethod(payment.getPaymentMethod())
                        .transactionReference(payment.getTransactionReference())
                        .note(payment.getNote())
                        .paymentDate(payment.getPaymentDate())
                        .paymentTime(payment.getPaymentTime())
                        .build())
                .toList();
    }

    private BillingResponseDto mapToResponse(
            BillingInfoEntity billingInfo,
            List<BillingItemEntity> items) {

        List<BillingItemResponseDto> itemResponses = items.stream()
                .map(item -> BillingItemResponseDto.builder()
                        .itemId(item.getItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .uom(item.getUom())
                        .quantity(item.getQuantity())
                        .sellingPrice(item.getSellingPrice())
                        .discount(item.getDiscount())
                        .totalAmount(item.getTotalAmount())
                        .build())
                .toList();

        return BillingResponseDto.builder()
                .billingId(billingInfo.getBillingId())
                .customerName(billingInfo.getCustomerName())
                .studentName(billingInfo.getStudentName() != null ? billingInfo.getStudentName() : billingInfo.getCustomerName())
                .fatherName(billingInfo.getFatherName())
                .className(billingInfo.getClassName())
                .studentClass(billingInfo.getClassName())
                .phoneNo(billingInfo.getPhoneNo())
                .otherNote(billingInfo.getOtherNote())
                .subtotal(billingInfo.getSubtotal())
                .totalDiscount(billingInfo.getTotalDiscount())
                .totalAmount(billingInfo.getTotalAmount())
                .amountPaid(billingInfo.getAmountPaid())
                .balance(billingInfo.getBalance())
                .totalItems(billingInfo.getTotalItems())
                .paymentStatus(billingInfo.getPaymentStatus())
                .status(billingInfo.getStatus())
                .billingDate(billingInfo.getBillingDate())
                .billingTime(billingInfo.getBillingTime())
                .items(itemResponses)
                .build();
    }
}
