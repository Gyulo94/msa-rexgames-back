package com.shop.payment.service;

import com.shop.common.error.ErrorCode;
import com.shop.common.exception.ApiException;
import com.shop.common.util.DistributedLockExecutor;
import com.shop.common.util.TransactionHelper;
import com.shop.payment.client.TossPaymentsClient;
import com.shop.payment.entity.Payment;
import com.shop.payment.event.PaymentEventPublisher;
import com.shop.payment.repository.PaymentRepository;
import com.shop.payment.request.PaymentConfirmRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final DistributedLockExecutor lockExecutor;
    private final TransactionHelper transactionHelper;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentEventPublisher eventPublisher;

    @Override
    public void confirmPayment(PaymentConfirmRequest request, int userId) {
        UUID orderId = request.getOrderId();
        String lockKey = "lock:payment:" + orderId.toString();

        log.debug("결제 락 획득 시도 중: orderId={}, amount={}", orderId, request.getAmount());

        lockExecutor.execute(lockKey, 5, 10, () -> {
            log.debug("결제 락 획득 성공: orderId={}", orderId);

            if (isPaymentAlreadyRegistered(orderId)) {
                return null;
            }

            try {
                tossPaymentsClient.confirm(request.getPaymentKey(), orderId.toString(), request.getAmount());

                transactionHelper.execute(() -> savePaymentAndPublishApproved(orderId, userId, request.getAmount(),
                        request.getPaymentKey()));
            } catch (HttpClientErrorException ex) {
                handlePgConfirmationError(orderId, userId, request, ex);
            } catch (Exception ex) {
                handleUnexpectedError(orderId, userId, ex);
            }
            return null;
        });
    }

    private boolean isPaymentAlreadyRegistered(UUID orderId) {
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.info("DB에 이미 등록된 결제 정보입니다. PG 승인 요청을 건너뜁니다: orderId={}", orderId);
            return true;
        }
        return false;
    }

    private void handlePgConfirmationError(UUID orderId, int userId, PaymentConfirmRequest request,
            HttpClientErrorException ex) {
        String errorResponse = ex.getResponseBodyAsString();
        log.error("토스페이먼츠 결제 승인 실패: orderId={}, status={}, response={}",
                orderId, ex.getStatusCode(), errorResponse);

        if (errorResponse.contains("ALREADY_APPROVED") || errorResponse.contains("ALREADY_COMPLETED")) {
            log.warn("PG사에서 이미 승인/완료된 결제로 확인되었습니다. DB 상태를 동기화합니다: orderId={}", orderId);
            transactionHelper.execute(
                    () -> savePaymentAndPublishApproved(orderId, userId, request.getAmount(), request.getPaymentKey()));
        } else {
            transactionHelper.execute(() -> eventPublisher.publishFailed(orderId, userId, errorResponse));
            throw new ApiException(ErrorCode.SERVER_ERROR, "결제 승인 API 호출 실패: " + errorResponse + ex);
        }
    }

    private void handleUnexpectedError(UUID orderId, int userId, Exception ex) {
        log.error("결제 승인 처리 중 예상치 못한 오류 발생: orderId={}", orderId, ex);
        transactionHelper.execute(() -> eventPublisher.publishFailed(orderId, userId, ex.getMessage()));
        throw new ApiException(ErrorCode.SERVER_ERROR, "결제 처리 중 예상치 못한 오류 발생" + ex);
    }

    private void savePaymentAndPublishApproved(UUID orderId, int userId, int amount, String paymentKey) {
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.info("DB에 이미 등록된 결제 정보입니다. 중복 저장을 건너뜁니다: orderId={}", orderId);
            return;
        }

        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .paymentKey(paymentKey)
                .status("APPROVED")
                .build();
        paymentRepository.save(payment);
        log.info("결제 정보가 DB에 정상 저장 및 승인 처리되었습니다: orderId={}", orderId);

        eventPublisher.publishApproved(orderId, userId, amount, paymentKey);
    }

    @Override
    public void cancelPayment(UUID orderId, String reason) {
        String lockKey = "lock:payment:" + orderId.toString();

        lockExecutor.execute(lockKey, 5, 10, () -> {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                            "결제 정보를 찾을 수 없습니다. (orderId: " + orderId + ")"));

            if ("CANCELLED".equals(payment.getStatus())) {
                log.info("이미 취소된 결제 정보입니다: orderId={}", orderId);
                return null;
            }

            try {
                tossPaymentsClient.cancel(payment.getPaymentKey(), reason);

                transactionHelper.execute(() -> {
                    payment.setStatus("CANCELLED");
                    paymentRepository.save(payment);
                    log.info("결제가 성공적으로 취소(환불) 처리되었습니다: orderId={}", orderId);
                    return null;
                });
            } catch (Exception ex) {
                log.error("토스페이먼츠 결제 취소 API 호출 실패: orderId={}", orderId, ex);
                throw new ApiException(ErrorCode.SERVER_ERROR, "결제 취소 처리 실패" + ex);
            }
            return null;
        });
    }
}
