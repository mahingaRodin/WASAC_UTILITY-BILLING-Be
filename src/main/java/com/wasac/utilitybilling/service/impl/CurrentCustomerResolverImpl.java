package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentCustomerResolverImpl implements CurrentCustomerResolver {
    private final CustomerRepository customerRepository;

    @Override
    public Customer resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile linked to this account."));
    }
}
