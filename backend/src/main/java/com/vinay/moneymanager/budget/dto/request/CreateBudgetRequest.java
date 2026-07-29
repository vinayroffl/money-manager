package com.vinay.moneymanager.budget.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Month;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBudgetRequest {
  @NotNull
  @Min(1)
  private Integer categoryId;

  @NotNull
  @DecimalMin("0.01")
  private BigDecimal amount;

  @NotNull private Month month;

  @NotNull
  @Min(2026)
  @Max(2999)
  private Integer year;
}
