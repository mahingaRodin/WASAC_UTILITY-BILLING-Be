package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.domain.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    Optional<MeterReading> findByMeterAndReadingDate(Meter meter, LocalDate readingDate);
}
