package com.java_ne_practical_tplt.payloads.requests;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
