package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.dto.CustomerRequest;

public interface CustomerService {
    Customer create(CustomerRequest request);
}
