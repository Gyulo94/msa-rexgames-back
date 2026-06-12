package com.shop.product_command.product.response;

import lombok.*;
import com.shop.product_command.product.entity.Product;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
  private Integer id;
  private String name;
  private String slug;
  private Integer price;
  private Integer genreId;
  private Integer platformId;
  private String genreName;
  private String platformName;
  private Integer discountPrice;
  private Integer discount;
  private String description;
  private double rating;
  private Integer stock;
  private boolean isOutOfStock;
  private Map<String, Object> specs;
  private List<String> images;
  private LocalDateTime createdAt;

  static public ProductResponse fromEntity(@NonNull Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .slug(product.getSlug())
        .price(product.getPrice())
        .genreId(product.getGenre().getId())
        .platformId(product.getPlatform().getId())
        .discount(product.getDiscount())
        .discountPrice(product.getDiscountPrice())
        .description(product.getDescription())
        .stock(product.getStock())
        .isOutOfStock(product.getStock() <= 0)
        .rating(product.getReviewRating())
        .genreName(product.getGenre().getName())
        .platformName(product.getPlatform().getName())
        .specs(product.getSpecs())
        .images(product.getImages())
        .createdAt(product.getCreatedAt())
        .build();
  }
}
