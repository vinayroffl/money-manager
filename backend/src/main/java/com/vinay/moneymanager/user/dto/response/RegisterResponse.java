package com.vinay.moneymanager.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
  UUID id;
  String email;
  String firstName;
  String lastName;
  LocalDateTime createdAt;
}
