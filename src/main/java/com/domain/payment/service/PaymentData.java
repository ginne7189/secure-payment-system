package com.domain.payment.service;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentData {
    private String paymentKey;
    private String orderId;
    private String status;
    private int amount;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
}
