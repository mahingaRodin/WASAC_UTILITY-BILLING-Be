package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Payment> findByBillReferenceOrderByCreatedAtDesc(String billReference);
    Page<Payment> findByBill_Customer_EmailOrderByCreatedAtDesc(String email, Pageable pageable);
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);
    List<Payment> findByBill_IdAndStatus(UUID billId, PaymentStatus status);
    Optional<Payment> findTopByBill_IdAndStatusOrderByPaymentDateDesc(UUID billId, PaymentStatus status);
}
