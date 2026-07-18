package com.vinay.moneymanager.user.controller;

import com.vinay.moneymanager.common.response.ApiResponse;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;
import com.vinay.moneymanager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/api/users/me")
  public ResponseEntity<ApiResponse<RegisterResponse>> me(Authentication authentication) {
    String username = authentication.getName();
    RegisterResponse basicUserDetails = userService.getBasicUserDetails(username);
    ApiResponse<RegisterResponse> response =
        ApiResponse.<RegisterResponse>builder()
            .success(true)
            .message("User Data Fetched Successfully!")
            .data(basicUserDetails)
            .build();
    return ResponseEntity.ok(response);
  }
}
