package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.MeterReading;
import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.MeterStatus;
import com.wasac.utilitybilling.dto.MeterReadingResponse;
import com.wasac.utilitybilling.dto.MeterReadingRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.mapper.MeterReadingMapper;
import com.wasac.utilitybilling.repository.MeterReadingRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.repository.UserRepository;
import com.wasac.utilitybilling.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {
    private final MeterRepository meterRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final MeterReadingMapper meterReadingMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MeterReadingResponse create(MeterReadingRequest request) {
        var meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found."));

        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BadRequestException("Meter must be active.");
        }
        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BadRequestException("Current reading must be greater than previous reading.");
        }
        meterReadingRepository.findTopByMeter_IdOrderByReadingDateDesc(request.getMeterId())
                .ifPresent(lastReading -> {
                    if (request.getPreviousReading().compareTo(lastReading.getCurrentReading()) != 0) {
                        throw new BadRequestException("Previous reading must match last recorded current reading.");
                    }
                });
        var monthStart = request.getReadingDate().withDayOfMonth(1);
        if (meterReadingRepository.findByMeterAndReadingDate(meter, monthStart).isPresent()) {
            throw new BadRequestException("Only one reading per meter per month/year is allowed.");
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(monthStart)
                .recordedBy(currentUserOrNull())
                .build();
        return meterReadingMapper.toResponse(meterReadingRepository.save(reading));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterReadingResponse> list(Pageable pageable) {
        return meterReadingRepository.findAllByOrderByReadingDateDesc(pageable).map(meterReadingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MeterReadingResponse getById(UUID id) {
        MeterReading reading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found."));
        return meterReadingMapper.toResponse(reading);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getByMeter(UUID meterId) {
        if (!meterRepository.existsById(meterId)) {
            throw new ResourceNotFoundException("Meter not found.");
        }
        return meterReadingRepository.findByMeter_IdOrderByReadingDateDesc(meterId).stream()
                .map(meterReadingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterReadingResponse> getMyReadings(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        return meterReadingRepository.findByRecordedBy_EmailOrderByReadingDateDesc(email, pageable)
                .map(meterReadingMapper::toResponse);
    }

    private User currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}
