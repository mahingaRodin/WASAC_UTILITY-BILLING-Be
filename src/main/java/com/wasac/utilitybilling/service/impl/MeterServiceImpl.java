package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.dto.MeterRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {
    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Meter create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new BadRequestException("Meter number already exists.");
        }
        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));

        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .type(request.getType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus())
                .customer(customer)
                .build();
        return meterRepository.save(meter);
    }
}
