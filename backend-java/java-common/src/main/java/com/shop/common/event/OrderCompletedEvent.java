package com.shop.common.event;

import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCompletedEvent {
    private UUID orderId;
    private Integer userId;
    private List<OrderItemInfo> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemInfo {
        private Integer productId;
        private Integer quantity;
    }
}
