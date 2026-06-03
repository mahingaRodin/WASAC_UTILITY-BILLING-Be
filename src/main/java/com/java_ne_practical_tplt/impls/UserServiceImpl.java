package com.java_ne_practical_tplt.impls;

import com.java_ne_practical_tplt.mappers.UserMapper;
import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.repositories.UserRepository;
import com.java_ne_practical_tplt.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDTO findByEmail(String email) throws Exception{
        User user = userRepository.findByEmail(email);
        if(user==null) {
            throw new Exception("User with given email does not exist!");
        }
        return UserMapper.toDTO(user);
    }

    @Override
    public UserDTO getUserById(UUID id) throws Exception{
        User user =userRepository.findById(id).orElseThrow(
                () -> new Exception("User with given id does not exist!")
        );
        return UserMapper.toDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.toDtoList(users);
    }
}
