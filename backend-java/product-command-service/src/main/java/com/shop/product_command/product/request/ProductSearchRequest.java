package com.shop.product_command.product.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
  private String name;
  private List<String> genres;
  private List<String> platforms;
  private Integer minPrice;
  private Integer maxPrice;
  private Integer minDiscount;
  private Integer maxDiscount;
  private Double minRating;
  private String sortBy;
  private String sortOrder;
}
