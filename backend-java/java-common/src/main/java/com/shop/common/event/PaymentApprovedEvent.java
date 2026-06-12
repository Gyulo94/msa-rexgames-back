package com.shop.common.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentApprovedEvent {
    private UUID orderId;
    private Integer userId;
    private Integer amount;
    private String paymentKey;
}
