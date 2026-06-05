package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {
    boolean existsByMeterNumber(String meterNumber);
}
