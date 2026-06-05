package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.dto.MeterRequest;

public interface MeterService {
    Meter create(MeterRequest request);
}
