package com.wasac.utilitybilling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {
    @NotBlank
    private String province;
    @NotBlank
    private String district;
    @NotBlank
    private String sector;
    @NotBlank
    private String cell;
    @NotBlank
    private String village;
}
