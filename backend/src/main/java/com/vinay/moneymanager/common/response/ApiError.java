package com.vinay.moneymanager.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

  private boolean success = false;
  private String message;
  private LocalDateTime timestamp;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<ValidationError> errors;
}
