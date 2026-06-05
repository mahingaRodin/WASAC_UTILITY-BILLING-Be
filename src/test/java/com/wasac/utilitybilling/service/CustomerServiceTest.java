package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.CustomerRequest;
import com.wasac.utilitybilling.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.CustomerRepository customerRepository;
    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createShouldRejectExistingNationalId() {
        CustomerRequest request = new CustomerRequest();
        request.setNationalId("11999");
        when(customerRepository.existsByNationalId("11999")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> customerService.create(request));
    }
}
