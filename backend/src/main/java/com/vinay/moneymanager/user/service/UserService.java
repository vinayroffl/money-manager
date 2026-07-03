package com.vinay.moneymanager.user.service;

import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;

public interface UserService {
    RegisterResponse register(RegisterRequest request);
}
