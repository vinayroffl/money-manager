package com.vinay.moneymanager.budget.dto.response;

import java.math.BigDecimal;
import java.time.Month;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
  private UUID id;
  private String categoryName;
  private BigDecimal amount;
  private BigDecimal spentAmount;
  private BigDecimal remainingAmount;
  private Month month;
  private Integer year;
}
