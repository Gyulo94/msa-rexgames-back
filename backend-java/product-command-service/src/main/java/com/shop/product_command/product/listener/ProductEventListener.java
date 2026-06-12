package com.shop.product_command.product.listener;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.shop.common.event.ProductCreatedEvent;
import com.shop.common.event.ProductUpdatedEvent;
import com.shop.common.event.ProductDeletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProductCreatedEvent(ProductCreatedEvent event) {
    kafkaTemplate.send("product-topic", event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("카프카 이벤트 전송 실패 (productId: {}): {}", event.getProductId(), ex.getMessage(), ex);
          } else {
            log.info("카프카 이벤트 전송 성공: topic={}, partition={}, offset={}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProductUpdatedEvent(ProductUpdatedEvent event) {
    kafkaTemplate.send("product-topic", event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("카프카 업데이트 이벤트 전송 실패 (productId: {}): {}", event.getProductId(), ex.getMessage(), ex);
          } else {
            log.info("카프카 업데이트 이벤트 전송 성공: topic={}, partition={}, offset={}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProductDeletedEvent(ProductDeletedEvent event) {
    kafkaTemplate.send("product-topic", event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("카프카 삭제 이벤트 전송 실패 (productId: {}): {}", event.getProductId(), ex.getMessage(), ex);
          } else {
            log.info("카프카 삭제 이벤트 전송 성공: topic={}, partition={}, offset={}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }
}
