package com.shop.product_command.gameCode.entity;

import com.shop.product_command.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GameCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "code", nullable = false, unique = true, length = 255)
  private String code;

  @Column(name = "is_sold", nullable = false)
  @Builder.Default
  private Boolean isSold = false;

  @Column(name = "sold_at")
  private LocalDateTime soldAt;

  @Column(name = "user_id")
  private Integer userId;

  public void markAsSold(Integer userId) {
    this.isSold = true;
    this.soldAt = LocalDateTime.now();
    this.userId = userId;
  }
}