package com.java_ne_practical_tplt.payloads.responses;

import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
    private UserDTO user;
    private String title;

}
