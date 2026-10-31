package com.vinay.moneymanager.security.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.common.response.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final ZoneId ZONE_ID_INDIA = ZoneId.of("Asia/Kolkata");

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    ApiError apiError = new ApiError();
    apiError.setSuccess(false);
    apiError.setMessage(
        authException instanceof BadCredentialsException
            ? "Invalid email or password"
            : "Authentication required");
    apiError.setTimestamp(LocalDateTime.now(ZONE_ID_INDIA));

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    objectMapper.writeValue(response.getOutputStream(), apiError);
  }
}
