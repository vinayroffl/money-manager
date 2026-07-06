package com.vinay.moneymanager.user.controller;

import com.vinay.moneymanager.common.response.ApiResponse;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;
import com.vinay.moneymanager.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {

        RegisterResponse registerResponse = userService.register(request);

        ApiResponse<RegisterResponse> response = ApiResponse.<RegisterResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(registerResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
