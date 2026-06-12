package com.shop.product_command.product.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

import com.shop.product_command.product.entity.Product;
import com.shop.product_command.genre.entity.Genre;
import com.shop.product_command.platform.entity.Platform;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

  @NotBlank(message = "제목은 필수 입니다.")
  private String name;

  @NotBlank(message = "슬러그는 필수 입니다.")
  private String slug;

  @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
  private Integer price;

  @Max(value = 99, message = "할인율은 99 이하이어야 합니다.")
  @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
  private Integer discount;

  @Min(value = 0, message = "할인된 가격은 0 이상이어야 합니다.")
  private Integer discountPrice;

  @NotBlank(message = "설명은 필수 입니다.")
  private String description;

  @NotNull(message = "장르는 필수 입니다.")
  private Integer genreId;

  @NotNull(message = "플랫폼은 필수 입니다.")
  private Integer platformId;

  @NotNull(message = "출시일은 필수 입니다.")
  private Map<String, Object> specs;

  @NotNull(message = "이미지는 필수 입니다.")
  private List<String> images;

  @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
  private Integer stock;

  static public Product toEntity(ProductRequest request) {
    return Product.builder()
        .name(request.getName())
        .slug(request.getSlug())
        .price(request.getPrice())
        .discount(request.getDiscount())
        .discountPrice(request.getDiscountPrice())
        .description(request.getDescription())
        .genre(Genre.builder().id(request.getGenreId()).build())
        .platform(Platform.builder().id(request.getPlatformId()).build())
        .specs(request.getSpecs())
        .images(request.getImages())
        .stock(request.getStock() != null ? request.getStock() : 0)
        .build();
  }

}
