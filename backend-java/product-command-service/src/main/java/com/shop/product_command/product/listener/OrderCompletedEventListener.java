package com.shop.product_command.product.listener;

import com.shop.common.event.OrderCompletedEvent;
import com.shop.common.event.OrderDeliveryFailedEvent;
import com.shop.product_command.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompletedEventListener {

  private final ProductService productService;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @KafkaListener(topics = "order-completed", groupId = "product-service-consumer")
  public void handleOrderCompleted(OrderCompletedEvent event) {
    log.info("OrderCompletedEvent를 처리합니다: orderId={}, userId={}", event.getOrderId(), event.getUserId());
    try {
      productService.processOrderCompletion(event);
      log.info("OrderCompletedEvent 처리 완료: orderId={}", event.getOrderId());
    } catch (Exception e) {
      log.error("OrderCompletedEvent 처리 실패. 보상 트랜잭션(환불)을 개시합니다. orderId={}, error={}", 
          event.getOrderId(), e.getMessage(), e);
      publishDeliveryFailedEvent(event, e.getMessage());
    }
  }

  private void publishDeliveryFailedEvent(OrderCompletedEvent event, String reason) {
    OrderDeliveryFailedEvent failedEvent = OrderDeliveryFailedEvent.builder()
        .orderId(event.getOrderId())
        .userId(event.getUserId())
        .reason(reason)
        .build();

    kafkaTemplate.send("order-delivery-failed", failedEvent)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("order-delivery-failed 이벤트 발송 실패: orderId={}, error={}", event.getOrderId(), ex.getMessage());
          } else {
            log.info("order-delivery-failed 이벤트 발송 성공: orderId={}", event.getOrderId());
          }
        });
  }
}
