package com.java_ne_practical_tplt.services;

import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.payloads.responses.AuthResponse;

public interface AuthService {
    AuthResponse login(String email , String password) throws Exception;
    AuthResponse signup(UserDTO req) throws Exception;
}
