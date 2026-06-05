package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.CustomerRequest;
import com.wasac.utilitybilling.dto.UpdateCustomerRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
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
    @Mock
    private com.wasac.utilitybilling.repository.MeterRepository meterRepository;
    @Mock
    private CurrentCustomerResolver currentCustomerResolver;
    @Mock
    private com.wasac.utilitybilling.mapper.CustomerMapper customerMapper;
    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createShouldRejectExistingNationalId() {
        CustomerRequest request = new CustomerRequest();
        request.setNationalId("11999");
        when(customerRepository.existsByNationalId("11999")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> customerService.create(request));
    }

    @Test
    void updateShouldRejectExistingEmailOnAnotherRecord() {
        java.util.UUID id = java.util.UUID.randomUUID();
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setEmail("duplicate@wasac.rw");
        request.setFullName("Updated");
        request.setPhone("+250700000001");
        request.setAddress(new com.wasac.utilitybilling.dto.AddressDTO());
        when(customerRepository.findById(id)).thenReturn(java.util.Optional.of(com.wasac.utilitybilling.domain.Customer.builder().id(id).build()));
        when(customerRepository.existsByEmailAndIdNot("duplicate@wasac.rw", id)).thenReturn(true);
        assertThrows(BadRequestException.class, () -> customerService.update(id, request));
    }

    @Test
    void deleteShouldRejectWhenCustomerHasMeters() {
        java.util.UUID id = java.util.UUID.randomUUID();
        when(meterRepository.existsByCustomer_Id(id)).thenReturn(true);
        assertThrows(BadRequestException.class, () -> customerService.delete(id));
    }
}
