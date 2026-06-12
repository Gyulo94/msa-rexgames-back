package com.shop.common.event;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdatedEvent {
  private Integer productId;
  private String name;
  private String slug;
  private Integer price;
  private Integer discount;
  private Integer discountPrice;
  private String genreName;
  private String platformName;
  private String description;
  private Double reviewRating;
  private Integer reviewCount;
  private Integer stock;
  private Map<String, Object> specs;
  private List<String> images;
}
