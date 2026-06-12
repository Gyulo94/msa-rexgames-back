package com.shop.product_command.product.entity;

import com.shop.product_command.genre.entity.Genre;
import com.shop.product_command.platform.entity.Platform;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

@Entity
@Table(name = "products")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(nullable = false, unique = true, length = 255)
  private String slug;

  @Column(nullable = false)
  private Integer price;

  private Integer discount;

  @Column(name = "discount_price")
  private Integer discountPrice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "genre_id")
  private Genre genre;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "platform_id")
  private Platform platform;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "review_rating", nullable = false)
  @Builder.Default
  private Double reviewRating = 0.0;

  @Column(name = "review_count", nullable = false)
  @Builder.Default
  private Integer reviewCount = 0;

  @Column(nullable = false)
  @Builder.Default
  private Integer stock = 0;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", nullable = false)
  private Map<String, Object> specs;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", nullable = false)
  private List<String> images;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "is_deleted", nullable = false)
  @Builder.Default
  private Boolean isDeleted = false;

  public void setDiscountPrice(Integer discountPrice) {
    this.discountPrice = discountPrice;
  }

  public void updateImages(List<String> images) {
    this.images = images;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  public void delete() {
    this.isDeleted = true;
  }

  public void update(Product product) {
    this.name = product.getName();
    this.slug = product.getSlug();
    this.price = product.getPrice();
    this.discount = product.getDiscount();
    this.discountPrice = product.getDiscountPrice();
    this.genre = product.getGenre();
    this.platform = product.getPlatform();
    this.specs = product.getSpecs();
    this.stock = product.getStock();
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }
}
