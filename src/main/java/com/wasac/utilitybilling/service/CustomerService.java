package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.CustomerRequest;
import com.wasac.utilitybilling.dto.CustomerResponse;
import com.wasac.utilitybilling.dto.UpdateCustomerRequest;
import com.wasac.utilitybilling.dto.UpdateCustomerStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    Page<CustomerResponse> list(Pageable pageable);
    CustomerResponse getById(UUID id);
    CustomerResponse update(UUID id, UpdateCustomerRequest request);
    CustomerResponse updateStatus(UUID id, UpdateCustomerStatusRequest request);
    void delete(UUID id);
    CustomerResponse getMyProfile();
}
