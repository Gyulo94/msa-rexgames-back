package com.shop.product_command.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "platforms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Platform {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;
}