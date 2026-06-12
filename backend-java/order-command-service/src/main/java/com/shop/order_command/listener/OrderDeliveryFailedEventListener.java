package com.shop.order_command.listener;

import com.shop.common.event.OrderDeliveryFailedEvent;
import com.shop.common.event.PaymentCancelRequestedEvent;
import com.shop.common.util.TransactionHelper;
import com.shop.order_command.entity.Order;
import com.shop.order_command.entity.OrderItem;
import com.shop.order_command.entity.OrderStatus;
import com.shop.order_command.repository.OrderRepository;
import com.shop.order_command.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDeliveryFailedEventListener {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TransactionHelper transactionHelper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-delivery-failed", groupId = "order-service-consumer")
    public void handleOrderDeliveryFailed(OrderDeliveryFailedEvent event) {
        log.warn("상품 인도 실패 이벤트를 처리합니다. 보상 트랜잭션 개시: orderId={}, reason={}", 
                event.getOrderId(), event.getReason());

        transactionHelper.execute(() -> {
            orderRepository.findById(event.getOrderId()).ifPresentOrElse(
                    order -> {
                        if (order.getStatus() == OrderStatus.PAID) {
                            cancelOrderAndRestoreStock(order, event.getReason());
                        } else {
                            log.warn("주문 상태가 PAID가 아닙니다. 취소 및 재고 복원을 건너뜁니다: orderId={}, 현재 상태={}", 
                                    order.getId(), order.getStatus());
                        }
                    },
                    () -> log.error("주문을 찾을 수 없습니다. orderId={}", event.getOrderId())
            );
            return null;
        });
    }

    private void cancelOrderAndRestoreStock(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.debug("주문 상태가 CANCELLED로 변경되었습니다: orderId={}", order.getId());

        for (OrderItem item : order.getOrderItems()) {
            int updatedRows = productRepository.restoreStock(item.getProductId(), item.getQuantity());
            if (updatedRows > 0) {
                log.debug("재고가 원자적으로 복원되었습니다 (보상 트랜잭션). productId={}, 수량={}", 
                        item.getProductId(), item.getQuantity());
            } else {
                log.error("재고 복원 실패. 상품을 찾을 수 없음: productId={}", item.getProductId());
            }
        }

        publishPaymentCancelRequestedEvent(order, reason);
    }

    private void publishPaymentCancelRequestedEvent(Order order, String reason) {
        PaymentCancelRequestedEvent cancelRequestedEvent = PaymentCancelRequestedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .reason(reason)
                .build();

        transactionHelper.runAfterCommit(() ->
            kafkaTemplate.send("payment-cancel-requested", cancelRequestedEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("payment-cancel-requested 이벤트 발송 실패: orderId={}, error={}", 
                                    order.getId(), ex.getMessage());
                        } else {
                            log.debug("payment-cancel-requested 이벤트 발송 성공: orderId={}", order.getId());
                        }
                    })
        );
    }
}
