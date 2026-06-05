package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.MeterReading;
import com.wasac.utilitybilling.dto.MeterReadingRequest;

public interface MeterReadingService {
    MeterReading create(MeterReadingRequest request);
}
