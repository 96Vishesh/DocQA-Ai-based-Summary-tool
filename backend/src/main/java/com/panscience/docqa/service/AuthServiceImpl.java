package com.panscience.docqa.service;

import com.panscience.docqa.dto.AuthRequest;
import com.panscience.docqa.dto.AuthResponse;
import com.panscience.docqa.dto.RegisterRequest;
import com.panscience.docqa.entity.User;
import com.panscience.docqa.repository.UserRepository;
import com.panscience.docqa.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
}
