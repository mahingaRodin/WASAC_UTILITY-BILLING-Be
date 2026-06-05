package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.dto.MeterResponse;
import com.wasac.utilitybilling.dto.MeterRequest;
import com.wasac.utilitybilling.dto.UpdateMeterStatusRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.mapper.MeterMapper;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.repository.MeterReadingRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.service.CurrentCustomerResolver;
import com.wasac.utilitybilling.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {
    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final BillRepository billRepository;
    private final CurrentCustomerResolver currentCustomerResolver;
    private final MeterMapper meterMapper;

    @Override
    @Transactional
    public MeterResponse create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new BadRequestException("Meter number already exists.");
        }
        if (request.getInstallationDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Installation date cannot be in the future.");
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
        return meterMapper.toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterResponse> list(Pageable pageable) {
        return meterRepository.findAllByOrderByCreatedAtDesc(pageable).map(meterMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MeterResponse getById(UUID id) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found."));
        return meterMapper.toResponse(meter);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterResponse> getByCustomerId(UUID customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found.");
        }
        return meterRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId, pageable).map(meterMapper::toResponse);
    }

    @Override
    @Transactional
    public MeterResponse updateStatus(UUID id, UpdateMeterStatusRequest request) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found."));
        meter.setStatus(request.getStatus());
        return meterMapper.toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found."));
        if (meterReadingRepository.existsByMeter_Id(id) || billRepository.existsByMeter_Id(id)) {
            throw new BadRequestException("Cannot delete meter with readings or bills.");
        }
        meterRepository.delete(meter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterResponse> getMyMeters() {
        String email = currentCustomerResolver.resolve().getEmail();
        return meterRepository.findByCustomer_EmailOrderByCreatedAtDesc(email).stream()
                .map(meterMapper::toResponse)
                .toList();
    }
}
