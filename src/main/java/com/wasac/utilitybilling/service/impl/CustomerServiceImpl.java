package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Address;
import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.dto.CustomerRequest;
import com.wasac.utilitybilling.dto.CustomerResponse;
import com.wasac.utilitybilling.dto.UpdateCustomerRequest;
import com.wasac.utilitybilling.dto.UpdateCustomerStatusRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.mapper.CustomerMapper;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.service.CurrentCustomerResolver;
import com.wasac.utilitybilling.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;
    private final CurrentCustomerResolver currentCustomerResolver;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new BadRequestException("National ID already exists.");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Customer email already exists.");
        }

        Customer customer = Customer.builder()
                .nationalId(request.getNationalId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(request.getStatus())
                .address(Address.builder()
                        .province(request.getAddress().getProvince())
                        .district(request.getAddress().getDistrict())
                        .sector(request.getAddress().getSector())
                        .cell(request.getAddress().getCell())
                        .village(request.getAddress().getVillage())
                        .build())
                .build();
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepository.findAllByOrderByCreatedAtDesc(pageable).map(customerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));

        if (customerRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BadRequestException("Customer email already exists.");
        }

        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(Address.builder()
                .province(request.getAddress().getProvince())
                .district(request.getAddress().getDistrict())
                .sector(request.getAddress().getSector())
                .cell(request.getAddress().getCell())
                .village(request.getAddress().getVillage())
                .build());
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse updateStatus(UUID id, UpdateCustomerStatusRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        customer.setStatus(request.getStatus());
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (meterRepository.existsByCustomer_Id(id)) {
            throw new BadRequestException("Cannot delete customer with existing meters.");
        }
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        customerRepository.delete(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getMyProfile() {
        return customerMapper.toResponse(currentCustomerResolver.resolve());
    }
}
