package com.vinay.moneymanager.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.common.exception.DuplicateResourceException;
import com.vinay.moneymanager.common.exception.GlobalExceptionHandler;
import com.vinay.moneymanager.config.SecurityConfig;
import com.vinay.moneymanager.security.entrypoint.JwtAuthenticationEntryPoint;
import com.vinay.moneymanager.security.service.CustomUserDetailsService;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;
import com.vinay.moneymanager.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private static RegisterRequest createRegisterRequest() {
        return RegisterRequest.builder()
                .email("vinay@test.com")
                .password("Password@123")
                .firstName("Vinay")
                .lastName("R")
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = createRegisterRequest();

        RegisterResponse response = new RegisterResponse();

        response.setId(UUID.randomUUID());
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());

        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("vinay@test.com"))
                .andExpect(jsonPath("$.data.firstName").value("Vinay"))
                .andExpect(jsonPath("$.data.lastName").value("R"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = createRegisterRequest();

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "User with email " + request.getEmail() + " already exists"));

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("User with email vinay@test.com already exists"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {

        RegisterRequest request = new RegisterRequest();


        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty());

        verifyNoInteractions(userService);

    }
}
