package com.java_ne_practical_tplt.payloads.dtos;

import com.java_ne_practical_tplt.payloads.enums.EUserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private EUserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
