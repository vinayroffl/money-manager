package com.vinay.moneymanager.budget.entity;

import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "budgets",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_budget_user_category_month_year",
          columnNames = {"user_id", "category_id", "month", "year"})
    })
public class Budget {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @NotNull
  @Column(nullable = false, precision = 19, scale = 2)
  @DecimalMin(value = "0.01")
  private BigDecimal amount;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Month month;

  @NotNull
  @Column(nullable = false)
  private Integer year;

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
