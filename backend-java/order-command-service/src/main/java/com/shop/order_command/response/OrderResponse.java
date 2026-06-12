package com.shop.order_command.response;

import java.util.UUID;

import com.shop.order_command.entity.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UUID id;
    private Integer userId;
    private String orderName;
    private Integer totalPrice;
    private String status;

    static public OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderName(order.getOrderName())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .build();
    }
}
