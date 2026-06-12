package com.shop.payment.event;

import com.shop.common.event.PaymentApprovedEvent;
import com.shop.common.event.PaymentFailedEvent;
import com.shop.common.util.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionHelper transactionHelper;

    public void publishApproved(UUID orderId, int userId, int amount, String paymentKey) {
        PaymentApprovedEvent approvedEvent = PaymentApprovedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .paymentKey(paymentKey)
                .build();

        transactionHelper.runAfterCommit(() -> kafkaTemplate.send("payment-approved", approvedEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("결제 승인 이벤트 전송 실패: orderId={}, error={}", orderId, ex.getMessage());
                    } else {
                        log.debug("결제 승인 이벤트 전송 성공: orderId={}", orderId);
                    }
                }));
    }

    public void publishFailed(UUID orderId, int userId, String errorMessage) {
        PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .errorMessage(errorMessage)
                .build();

        transactionHelper.runAfterCommit(() -> kafkaTemplate.send("payment-failed", failedEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("결제 실패 이벤트 전송 실패: orderId={}, error={}", orderId, ex.getMessage());
                    } else {
                        log.debug("결제 실패 이벤트 전송 성공: orderId={}", orderId);
                    }
                }));
    }
}
