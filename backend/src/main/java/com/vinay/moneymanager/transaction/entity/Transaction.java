package com.vinay.moneymanager.transaction.entity;

import com.vinay.moneymanager.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @NotNull
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @NotNull
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @Column(length = 500)
  private String description;

  @NotNull
  @Column(nullable = false)
  private LocalDateTime transactionDate;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
