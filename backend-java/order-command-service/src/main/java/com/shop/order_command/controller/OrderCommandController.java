package com.shop.order_command.controller;

import com.shop.common.annotations.CurrentUser;
import com.shop.common.api.Api;
import com.shop.common.jwt.JwtPayload;
import com.shop.common.message.ResponseMessage;
import com.shop.order_command.request.OrderRequest;
import com.shop.order_command.response.OrderResponse;
import com.shop.order_command.service.OrderCommandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("order-command")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    @PostMapping
    public Api<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @CurrentUser JwtPayload user) {
        OrderResponse response = orderCommandService.createOrder(request, user.getId());
        return Api.OK(response, ResponseMessage.CREATE_ORDER_SUCCESS);
    }

    @GetMapping("/{orderId}")
    public Api<OrderResponse> findOrderById(
            @PathVariable UUID orderId,
            @CurrentUser JwtPayload user) {
        OrderResponse response = orderCommandService.findOrderById(orderId, user.getId());
        return Api.OK(response);
    }
}
