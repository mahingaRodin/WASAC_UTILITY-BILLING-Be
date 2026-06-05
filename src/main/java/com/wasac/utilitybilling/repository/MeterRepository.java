package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Meter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {
    boolean existsByMeterNumber(String meterNumber);
    Page<Meter> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Meter> findByCustomer_IdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    List<Meter> findByCustomer_EmailOrderByCreatedAtDesc(String email);
    boolean existsByCustomer_Id(UUID customerId);
}
