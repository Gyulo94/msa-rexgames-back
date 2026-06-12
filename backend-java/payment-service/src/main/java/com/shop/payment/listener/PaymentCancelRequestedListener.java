package com.shop.payment.listener;

import com.shop.common.event.PaymentCancelRequestedEvent;
import com.shop.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelRequestedListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "payment-cancel-requested", groupId = "payment-service-consumer")
    public void handlePaymentCancelRequested(PaymentCancelRequestedEvent event) {
        log.warn("결제 취소(환불) 요청 이벤트를 수신했습니다. orderId={}, reason={}",
                event.getOrderId(), event.getReason());

        try {
            paymentService.cancelPayment(event.getOrderId(), event.getReason());
            log.info("결제 취소(환불) 요청 처리 완료: orderId={}", event.getOrderId());
        } catch (Exception ex) {
            log.error("결제 취소(환불) 처리 중 오류 발생: orderId={}", event.getOrderId(), ex);
            throw ex;
        }
    }
}
