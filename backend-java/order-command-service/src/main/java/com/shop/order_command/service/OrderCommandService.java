package com.shop.order_command.service;

import com.shop.order_command.request.OrderRequest;
import com.shop.order_command.response.OrderResponse;
import java.util.UUID;

public interface OrderCommandService {
    OrderResponse createOrder(OrderRequest request, int userId);

    OrderResponse findOrderById(UUID orderId, int userId);
}
