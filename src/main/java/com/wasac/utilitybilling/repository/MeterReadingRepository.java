package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.domain.MeterReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    Optional<MeterReading> findByMeterAndReadingDate(Meter meter, LocalDate readingDate);
    Page<MeterReading> findAllByOrderByReadingDateDesc(Pageable pageable);
    List<MeterReading> findByMeter_IdOrderByReadingDateDesc(UUID meterId);
    Optional<MeterReading> findTopByMeter_IdOrderByReadingDateDesc(UUID meterId);
    boolean existsByMeter_Id(UUID meterId);
    Page<MeterReading> findByRecordedBy_EmailOrderByReadingDateDesc(String email, Pageable pageable);
}
