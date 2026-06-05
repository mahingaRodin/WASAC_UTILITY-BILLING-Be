package com.java_ne_practical_tplt.mappers;

import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.dtos.UserDTO;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        if(user==null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .lastLogin(user.getLastLogin())
                .phone(user.getPhone())
                .build();
    }

    public static List<UserDTO> toDtoList(List<User> users) {
        return users.stream().map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }
}
