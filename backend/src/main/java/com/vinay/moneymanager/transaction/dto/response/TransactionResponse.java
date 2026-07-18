package com.vinay.moneymanager.transaction.dto.response;

import com.vinay.moneymanager.transaction.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

  private UUID id;

  private BigDecimal amount;

  private TransactionType type;

  private Integer categoryId;

  private String categoryName;

  private String description;

  private LocalDateTime transactionDate;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
