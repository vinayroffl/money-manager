package com.vinay.moneymanager.user.service.impl;

import com.vinay.moneymanager.common.exception.DuplicateResourceException;
import com.vinay.moneymanager.common.exception.ResourceNotFoundException;
import com.vinay.moneymanager.security.jwt.JwtService;
import com.vinay.moneymanager.user.dto.request.LoginRequest;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.LoginResponse;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;
import com.vinay.moneymanager.user.entity.User;
import com.vinay.moneymanager.user.repository.UserRepository;
import com.vinay.moneymanager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  public RegisterResponse register(RegisterRequest request) {

    boolean emailExists = userRepository.existsByEmail(request.getEmail());

    if (emailExists)
      throw new DuplicateResourceException(
          "User with email " + request.getEmail() + " already exists");

    String encodedPassword = passwordEncoder.encode(request.getPassword());

    User user =
        User.builder()
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

  @Override
  public LoginResponse login(LoginRequest request) {

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
    return LoginResponse.builder().token(token).tokenType("Bearer").build();
  }

  @Override
  public RegisterResponse getBasicUserDetails(String username) {

    User user =
        userRepository
            .findByEmail(username)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

    return RegisterResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
