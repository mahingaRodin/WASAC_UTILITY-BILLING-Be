package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.BillLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BillLineItemRepository extends JpaRepository<BillLineItem, UUID> {
    List<BillLineItem> findByBill_Id(UUID billId);
}
