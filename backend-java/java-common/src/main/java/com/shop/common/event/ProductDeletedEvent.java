package com.shop.common.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDeletedEvent {
  private Integer productId;
}
