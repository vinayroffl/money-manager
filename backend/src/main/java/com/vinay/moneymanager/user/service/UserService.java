package com.vinay.moneymanager.user.service;

import com.vinay.moneymanager.user.dto.request.LoginRequest;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.LoginResponse;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;
import jakarta.validation.Valid;

public interface UserService {
  RegisterResponse register(RegisterRequest request);

  LoginResponse login(@Valid LoginRequest request);

  RegisterResponse getBasicUserDetails(String username);
}
