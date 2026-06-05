package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Address;
import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.dto.CustomerRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer create(CustomerRequest request) {
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
        return customerRepository.save(customer);
    }
}
