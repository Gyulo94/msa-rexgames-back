package com.shop.order_command.listener;

import com.shop.common.event.OrderCompletedEvent;
import com.shop.common.event.PaymentApprovedEvent;
import com.shop.common.event.PaymentFailedEvent;
import com.shop.common.util.TransactionHelper;
import com.shop.order_command.entity.Order;
import com.shop.order_command.entity.OrderItem;
import com.shop.order_command.entity.OrderStatus;
import com.shop.order_command.repository.OrderRepository;
import com.shop.order_command.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentEventListener {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TransactionHelper transactionHelper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-approved", groupId = "order-service-consumer")
    public void handlePaymentApproved(PaymentApprovedEvent event) {
        log.debug("PaymentApprovedEvent를 처리합니다: orderId={}, amount={}", event.getOrderId(), event.getAmount());

        transactionHelper.execute(() -> {
            processPaymentApproval(event.getOrderId());
            return null;
        });
    }

    private void processPaymentApproval(UUID orderId) {
        orderRepository.findById(orderId).ifPresentOrElse(
                this::approveOrder,
                () -> log.error("주문을 찾을 수 없습니다. orderId={}", orderId));
    }

    private void approveOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("주문 상태가 PENDING_PAYMENT가 아닙니다. orderId={}, 현재 상태={}", order.getId(), order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.debug("주문 상태가 PAID로 변경되었습니다. orderId={}", order.getId());

        publishOrderCompletedEvent(order);
    }

    private void publishOrderCompletedEvent(Order order) {
        List<OrderCompletedEvent.OrderItemInfo> itemInfos = order.getOrderItems().stream()
                .map(item -> OrderCompletedEvent.OrderItemInfo.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderCompletedEvent completedEvent = OrderCompletedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .items(itemInfos)
                .build();

        transactionHelper.runAfterCommit(() -> kafkaTemplate.send("order-completed", completedEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("주문 완료 이벤트 전송 실패: orderId={}, error={}", order.getId(), ex.getMessage());
                    } else {
                        log.debug("주문 완료 이벤트 전송 성공: orderId={}", order.getId());
                    }
                }));
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-service-consumer")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.debug("PaymentFailedEvent를 처리합니다: orderId={}, error={}", event.getOrderId(), event.getErrorMessage());

        orderRepository.findById(event.getOrderId()).ifPresentOrElse(
                order -> {
                    if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                        restoreStockAndCancelOrder(order);
                    } else {
                        log.warn("주문 상태가 PENDING_PAYMENT가 아닙니다. orderId={}, 현재 상태={}", order.getId(),
                                order.getStatus());
                    }
                },
                () -> log.error("주문을 찾을 수 없습니다. orderId={}", event.getOrderId()));
    }

    private void restoreStockAndCancelOrder(Order order) {
        transactionHelper.execute(() -> {
            Order targetOrder = orderRepository.findById(order.getId())
                    .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

            if (targetOrder.getStatus() == OrderStatus.PENDING_PAYMENT) {
                targetOrder.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(targetOrder);

                for (OrderItem item : targetOrder.getOrderItems()) {
                    int updatedRows = productRepository.restoreStock(item.getProductId(), item.getQuantity());
                    if (updatedRows > 0) {
                        log.debug("재고가 원자적으로 복원되었습니다. productId={}, 추가 수량={}", item.getProductId(), item.getQuantity());
                    } else {
                        log.error("재고 복원 실패 (상품을 찾을 수 없음). productId={}", item.getProductId());
                    }
                }
                log.debug("주문 상태가 CANCELLED로 변경되고 재고가 복원되었습니다. orderId={}", targetOrder.getId());
            }
            return null;
        });
    }
}
