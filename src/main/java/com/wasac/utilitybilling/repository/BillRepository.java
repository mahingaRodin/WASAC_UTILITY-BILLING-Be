package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findByBillReference(String billReference);
}
