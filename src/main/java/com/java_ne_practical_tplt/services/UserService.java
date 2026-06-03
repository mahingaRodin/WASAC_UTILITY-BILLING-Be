package com.java_ne_practical_tplt.services;

import com.java_ne_practical_tplt.payloads.dtos.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO findByEmail(String email) throws Exception;
    UserDTO getUserById(UUID id) throws Exception;
    List<UserDTO> getAllUsers();
}
