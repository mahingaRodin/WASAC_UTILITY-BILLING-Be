package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.MeterRequest;
import com.wasac.utilitybilling.dto.MeterResponse;
import com.wasac.utilitybilling.dto.UpdateMeterStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MeterService {
    MeterResponse create(MeterRequest request);
    Page<MeterResponse> list(Pageable pageable);
    MeterResponse getById(UUID id);
    Page<MeterResponse> getByCustomerId(UUID customerId, Pageable pageable);
    MeterResponse updateStatus(UUID id, UpdateMeterStatusRequest request);
    void delete(UUID id);
    List<MeterResponse> getMyMeters();
}
