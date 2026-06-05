package com.wasac.utilitybilling.mapper;

import com.wasac.utilitybilling.domain.Address;
import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.dto.AddressDTO;
import com.wasac.utilitybilling.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .nationalId(customer.getNationalId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .status(customer.getStatus())
                .address(toAddressDto(customer.getAddress()))
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    private AddressDTO toAddressDto(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setProvince(address.getProvince());
        dto.setDistrict(address.getDistrict());
        dto.setSector(address.getSector());
        dto.setCell(address.getCell());
        dto.setVillage(address.getVillage());
        return dto;
    }
}
