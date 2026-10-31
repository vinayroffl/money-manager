package com.vinay.moneymanager.common.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void shouldMapResourceNotFoundTo404() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Transaction not found"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldMapDuplicateResourceTo409() throws Exception {
    mockMvc
        .perform(get("/test/duplicate"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Transaction already exists"));
  }

  @Test
  void shouldMapInvalidRequestTo400() throws Exception {
    mockMvc
        .perform(get("/test/invalid-request"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid request"));
  }

  @Test
  void shouldMapFeatureNotImplementedTo501() throws Exception {
    mockMvc
        .perform(get("/test/not-implemented"))
        .andExpect(status().isNotImplemented())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Not implemented yet"));
  }

  @Test
  void shouldMapValidationErrorsTo400() throws Exception {
    mockMvc
        .perform(
            post("/test/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].field").value("name"))
        .andExpect(jsonPath("$.errors[0].message").value("Name is required"));
  }

  @Test
  void shouldMapTypeMismatchTo400() throws Exception {
    mockMvc
        .perform(get("/test/type/not-a-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value("Invalid value for parameter 'id'. Expected type: UUID"));
  }

  @Test
  void shouldMapMalformedJsonTo400() throws Exception {
    mockMvc
        .perform(
            post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not valid json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
  }

  @Test
  void shouldMapMissingRequestParameterTo400() throws Exception {
    mockMvc
        .perform(get("/test/missing-param"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Missing required parameter 'required'"));
  }

  @Test
  void shouldMapMissingRequestHeaderTo400() throws Exception {
    mockMvc
        .perform(get("/test/header"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Required request value is missing"));
  }

  @Test
  void shouldMapUnsupportedMediaTypeTo415() throws Exception {
    mockMvc
        .perform(
            post("/test/body")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Unsupported media type"));
  }

  @Test
  void shouldMapUnsupportedMethodTo405() throws Exception {
    mockMvc
        .perform(post("/test/get-only"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value("HTTP method 'POST' is not supported for this endpoint"));
  }

  @Test
  void shouldMapDataIntegrityViolationTo409() throws Exception {
    mockMvc
        .perform(get("/test/data-integrity"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Request conflicts with existing data"));
  }

  @Test
  void shouldMapBadCredentialsTo401() throws Exception {
    mockMvc
        .perform(get("/test/bad-credentials"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void shouldMapAuthenticationExceptionTo401() throws Exception {
    mockMvc
        .perform(get("/test/authentication-required"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Authentication required"));
  }

  @Test
  void shouldMapAccessDeniedTo403() throws Exception {
    mockMvc
        .perform(get("/test/access-denied"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Access denied"));
  }

  @Test
  void shouldHideUnexpectedExceptionDetails() throws Exception {
    mockMvc
        .perform(get("/test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(content().string(not(containsString("secret internal detail"))));
  }

  @RestController
  @RequestMapping("/test")
  static class TestController {

    @GetMapping("/not-found")
    void notFound() {
      throw new ResourceNotFoundException("Transaction not found");
    }

    @GetMapping("/duplicate")
    void duplicate() {
      throw new DuplicateResourceException("Transaction already exists");
    }

    @GetMapping("/invalid-request")
    void invalidRequest() {
      throw new InvalidRequestException("Invalid request");
    }

    @GetMapping("/not-implemented")
    void notImplemented() {
      throw new FeatureNotImplementedException("Not implemented yet");
    }

    @PostMapping("/validated")
    void validated(@Valid @RequestBody ValidatedPayload payload) {}

    @GetMapping("/type/{id}")
    void typeMismatch(@PathVariable UUID id) {}

    @PostMapping("/body")
    void body(@RequestBody TestPayload payload) {}

    @GetMapping("/missing-param")
    void missingParam(@RequestParam String required) {}

    @GetMapping("/header")
    void header(@RequestHeader("X-Test") String value) {}

    @GetMapping("/get-only")
    void getOnly() {}

    @GetMapping("/data-integrity")
    void dataIntegrity() {
      throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
    }

    @GetMapping("/bad-credentials")
    void badCredentials() {
      throw new BadCredentialsException("Bad credentials");
    }

    @GetMapping("/authentication-required")
    void authenticationRequired() {
      throw new InsufficientAuthenticationException("Authentication required");
    }

    @GetMapping("/access-denied")
    void accessDenied() {
      throw new AccessDeniedException("Not allowed");
    }

    @GetMapping("/unexpected")
    void unexpected() {
      throw new IllegalStateException("secret internal detail");
    }
  }

  static class ValidatedPayload {

    @NotBlank(message = "Name is required")
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  record TestPayload(String name) {}
}
