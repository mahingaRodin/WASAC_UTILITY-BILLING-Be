package com.java_ne_practical_tplt.impls;

import com.java_ne_practical_tplt.configs.JwtProvider;
import com.java_ne_practical_tplt.mappers.UserMapper;
import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.payloads.responses.AuthResponse;
import com.java_ne_practical_tplt.repositories.UserRepository;
import com.java_ne_practical_tplt.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public AuthResponse login(String email, String password) throws Exception {
        Authentication authentication =authenticate(email, password);

        User user = userRepository.findByEmail(email);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        String token = tokenProvider.generateToken(authentication, user.getId());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUser(UserMapper.toDTO(user));
        authResponse.setTitle("Welcome "+ user.getFirstName() +"!");
        authResponse.setMessage("You have successfully logged in!");
        return authResponse;
    }

    @Override
    public AuthResponse signup(UserDTO req) throws Exception {
        User existingUser = userRepository.findByEmail(req.getEmail());
        if(existingUser != null){
            throw new Exception("User Already Exists!");
        };
        User newUser = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(req.getRole())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .lastLogin(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getEmail(), savedUser.getPassword());
        String token = tokenProvider.generateToken(authentication, savedUser.getId());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUser(UserMapper.toDTO(savedUser));
        authResponse.setTitle("Welcome "+ savedUser.getFirstName() +"!");
        authResponse.setMessage("You have successfully registered!");
        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws Exception {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new Exception("Invalid Credentials!");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
