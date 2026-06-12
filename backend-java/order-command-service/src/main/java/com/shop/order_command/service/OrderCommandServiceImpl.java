package com.shop.order_command.service;

import com.shop.common.error.ErrorCode;
import com.shop.common.exception.ApiException;
import com.shop.common.util.DistributedLockExecutor;
import com.shop.common.util.TransactionHelper;
import com.shop.order_command.entity.Order;
import com.shop.order_command.entity.OrderItem;
import com.shop.order_command.entity.Product;
import com.shop.order_command.repository.OrderRepository;
import com.shop.order_command.repository.ProductRepository;
import com.shop.order_command.request.OrderRequest;
import com.shop.order_command.response.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final DistributedLockExecutor lockExecutor;
    private final TransactionHelper transactionHelper;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public OrderResponse createOrder(OrderRequest request, int userId) {
        List<String> lockKeys = request.getItems().stream()
                .map(item -> "lock:product:" + item.getProductId())
                .collect(Collectors.toList());

        Order order = lockExecutor.executeMulti(lockKeys, 5, 10,
                () -> transactionHelper.execute(() -> processOrderCreation(request, userId)));

        OrderResponse response = OrderResponse.fromEntity(order);
        return response;
    }

    private Order processOrderCreation(OrderRequest request, int userId) {
        Order order = OrderRequest.toEntity(request, userId);

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));
            if (product.getIsDeleted()) {
                throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "삭제된 상품입니다.");
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "상품 '" + product.getName() + "'의 재고가 부족합니다.");
            }
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
            OrderItem orderItem = OrderRequest.toEntity(itemRequest);
            order.addOrderItem(orderItem);
        }
        return orderRepository.save(order);
    }

    @Override
    public OrderResponse findOrderById(UUID orderId, int userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getUserId() != userId) {
            throw new ApiException(ErrorCode.FORBIDDEN, "해당 주문에 접근할 권한이 없습니다.");
        }

        return OrderResponse.fromEntity(order);
    }
}
