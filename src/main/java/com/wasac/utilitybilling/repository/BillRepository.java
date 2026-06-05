package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.enums.BillApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findByBillReference(String billReference);
    Page<Bill> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Bill> findByApprovalStatusOrderByCreatedAtDesc(BillApprovalStatus approvalStatus, Pageable pageable);
    Page<Bill> findByCustomer_IdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    Page<Bill> findByCustomer_EmailOrderByCreatedAtDesc(String email, Pageable pageable);
    boolean existsByMeter_Id(UUID meterId);
    List<Bill> findByDueDateBetweenAndOutstandingBalanceGreaterThan(LocalDate from, LocalDate to, BigDecimal amount);
}
