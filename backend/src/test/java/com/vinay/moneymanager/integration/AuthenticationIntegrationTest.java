package com.vinay.moneymanager.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.common.response.ApiResponse;
import com.vinay.moneymanager.user.dto.request.LoginRequest;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.LoginResponse;
import com.vinay.moneymanager.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  private RegisterRequest createRegisterRequest() {
    return RegisterRequest.builder()
        .email("vinay@test.com")
        .password("Password@123")
        .firstName("Vinay")
        .lastName("R")
        .build();
  }

  private LoginRequest createLoginRequest(String email, String password) {
    return LoginRequest.builder().email(email).password(password).build();
  }

  @Test
  void shouldRegisterUserSuccessfully() throws Exception {
    RegisterRequest registerRequest = createRegisterRequest();
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value(registerRequest.getEmail()));

    assertTrue(userRepository.existsByEmail(registerRequest.getEmail()));
  }

  @Test
  void shouldNotRegisterDuplicateEmail() throws Exception {
    RegisterRequest registerRequest = createRegisterRequest();
    mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value("User with email " + registerRequest.getEmail() + " already exists"));

    assertTrue(userRepository.existsByEmail(registerRequest.getEmail()));
  }

  @Test
  void shouldLoginSuccessfully() throws Exception {
    RegisterRequest registerRequest = createRegisterRequest();
    mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

    assertTrue(userRepository.existsByEmail(registerRequest.getEmail()));

    LoginRequest loginRequest =
        createLoginRequest(registerRequest.getEmail(), registerRequest.getPassword());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Login successfully"))
        .andExpect(jsonPath("$.data.token").isNotEmpty())
        .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
  }

  @Test
  void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
    RegisterRequest registerRequest = createRegisterRequest();
    LoginRequest loginRequest =
        createLoginRequest(registerRequest.getEmail(), registerRequest.getPassword());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void shouldAccessProtectedEndpointWithValidJwt() throws Exception {
    RegisterRequest registerRequest = createRegisterRequest();
    mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

    assertTrue(userRepository.existsByEmail(registerRequest.getEmail()));

    LoginRequest loginRequest =
        createLoginRequest(registerRequest.getEmail(), registerRequest.getPassword());

    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Login successfully"))
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    ApiResponse<LoginResponse> apiResponse =
        objectMapper.readValue(responseBody, new TypeReference<>() {});
    String token = apiResponse.getData().getToken();

    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User Data Fetched Successfully!"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.email").value(registerRequest.getEmail()))
        .andExpect(jsonPath("$.data.firstName").value(registerRequest.getFirstName()))
        .andExpect(jsonPath("$.data.lastName").value(registerRequest.getLastName()));
  }

  @Test
  void shouldReturnUnauthorizedWithoutJwt() throws Exception {

    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void shouldReturnUnauthorizedWithInvalidJwt() throws Exception {

    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer invalid.jwt.token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }
}
