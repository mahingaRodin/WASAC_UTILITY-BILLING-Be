package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.MeterReading;
import com.wasac.utilitybilling.domain.enums.MeterStatus;
import com.wasac.utilitybilling.dto.MeterReadingRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.MeterReadingRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {
    private final MeterRepository meterRepository;
    private final MeterReadingRepository meterReadingRepository;

    @Override
    @Transactional
    public MeterReading create(MeterReadingRequest request) {
        var meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found."));

        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BadRequestException("Meter must be active.");
        }
        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BadRequestException("Current reading must be greater than previous reading.");
        }
        var monthStart = request.getReadingDate().withDayOfMonth(1);
        if (meterReadingRepository.findByMeterAndReadingDate(meter, monthStart).isPresent()) {
            throw new BadRequestException("Only one reading per meter per month/year is allowed.");
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(monthStart)
                .build();
        return meterReadingRepository.save(reading);
    }
}
