package com.vinay.moneymanager.user.service.impl;

import com.vinay.moneymanager.common.exception.DuplicateResourceException;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;
import com.vinay.moneymanager.user.entity.User;
import com.vinay.moneymanager.user.repository.UserRepository;
import com.vinay.moneymanager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        boolean emailExists = userRepository.existsByEmail(request.getEmail());

        if (emailExists)
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        userRepository.save(user);

        return RegisterResponse.builder()
                .id(user.getId())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
