package com.shop.order_command.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.shop.order_command.entity.Order;
import com.shop.order_command.entity.OrderItem;
import com.shop.order_command.entity.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private List<OrderItemRequest> items;
    private Integer totalPrice;
    private String couponId;
    private String orderName;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        private Integer productId;
        private String name;
        private Integer price;
        private Integer quantity;
    }

    static public Order toEntity(OrderRequest request, int userId) {
        return Order.builder()
                .userId(userId)
                .orderName(request.getOrderName())
                .totalPrice(request.getTotalPrice())
                .couponId(request.getCouponId())
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
    }

    static public OrderItem toEntity(OrderItemRequest request) {
        return OrderItem.builder()
                .productId(request.getProductId())
                .name(request.getName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
    }
}
