package com.shop.payment.service;

import com.shop.payment.request.PaymentConfirmRequest;
import java.util.UUID;

public interface PaymentService {
    void confirmPayment(PaymentConfirmRequest request, int userId);
    void cancelPayment(UUID orderId, String reason);
}
