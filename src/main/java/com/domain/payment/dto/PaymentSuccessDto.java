package com.domain.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSuccessDto {
    private String paymentKey;
    private String orderId;
    private Long amount;
    private String status;  // 결제 상태 (예: SUCCESS, FAIL)
    private String method;  // 결제 수단 (예: 카드, 간편결제)
    private String approvedAt; // 결제 승인 시간
}
