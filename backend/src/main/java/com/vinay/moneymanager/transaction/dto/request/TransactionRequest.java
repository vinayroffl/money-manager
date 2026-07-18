package com.vinay.moneymanager.transaction.dto.request;

import com.vinay.moneymanager.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
  @Digits(
      integer = 19,
      fraction = 2,
      message = "Amount can have up to 19 digits and 2 decimal places")
  private BigDecimal amount;

  @NotNull(message = "Transaction type is required")
  private TransactionType type;

  @NotNull(message = "Category is required")
  private Integer categoryId;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;

  private LocalDateTime transactionDate;
}
