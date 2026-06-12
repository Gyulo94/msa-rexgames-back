package com.shop.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.shop.common.constants.Constants;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {
    private final Constants constants;
    private final RestTemplate restTemplate = new RestTemplate();

    public void confirm(String paymentKey, String orderId, int amount) {
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.debug("토스페이먼츠 결제 승인 API 호출 중: orderId={}", orderId);
        restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/confirm",
                entity,
                Map.class);
        log.debug("토스페이먼츠 결제 승인 API 호출 성공: orderId={}", orderId);
    }

    public void cancel(String paymentKey, String reason) {
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", reason);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.debug("토스페이먼츠 결제 취소 API 호출 중: paymentKey={}", paymentKey);
        restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel",
                entity,
                Map.class);
        log.debug("토스페이먼츠 결제 취소 API 호출 성공: paymentKey={}", paymentKey);
    }

    private HttpHeaders createHeaders() {
        String authString = constants.getTossSecretKey() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
        String authHeader = "Basic " + encodedAuth;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
