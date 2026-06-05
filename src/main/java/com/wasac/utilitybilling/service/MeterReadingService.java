package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.MeterReadingRequest;
import com.wasac.utilitybilling.dto.MeterReadingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MeterReadingService {
    MeterReadingResponse create(MeterReadingRequest request);
    Page<MeterReadingResponse> list(Pageable pageable);
    MeterReadingResponse getById(UUID id);
    List<MeterReadingResponse> getByMeter(UUID meterId);
    Page<MeterReadingResponse> getMyReadings(Pageable pageable);
}
