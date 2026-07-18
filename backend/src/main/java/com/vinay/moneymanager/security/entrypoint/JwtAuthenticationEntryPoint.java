package com.vinay.moneymanager.security.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.common.response.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    ApiError apiError = new ApiError();
    apiError.setSuccess(false);
    apiError.setMessage("Invalid email or password");
    apiError.setTimestamp(LocalDateTime.now());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    objectMapper.writeValue(response.getOutputStream(), apiError);
  }
}
