package com.vinay.moneymanager.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Please enter a valid email address")
  private String email;

  @NotNull
  @NotBlank(message = "First name is required")
  @Size(min = 3, max = 255, message = "Please enter valid First name")
  private String firstName;

  @NotNull
  @NotBlank(message = "Last name is required")
  @Size(min = 1, max = 255, message = "Please enter valid Last name")
  private String lastName;

  @NotNull
  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 36, message = "Password must be between 8 and 36 characters")
  private String password;
}
