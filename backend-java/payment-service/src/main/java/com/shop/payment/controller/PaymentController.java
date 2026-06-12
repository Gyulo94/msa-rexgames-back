package com.shop.payment.controller;

import com.shop.common.annotations.CurrentUser;
import com.shop.common.api.Api;
import com.shop.common.jwt.JwtPayload;
import com.shop.common.message.ResponseMessage;
import com.shop.payment.request.PaymentConfirmRequest;
import com.shop.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("confirm")
    public Api<Void> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @CurrentUser JwtPayload user) {
        paymentService.confirmPayment(request, user.getId().intValue());
        return Api.OK(ResponseMessage.PAYMENT_SUCCESS);
    }
}
