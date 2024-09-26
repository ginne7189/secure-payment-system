package com.domain.payment.controller;

import com.domain.payment.dto.PaymentSuccessDto;
import com.domain.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/toss")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // GET 요청 처리 (customerId 제거 및 링크에 맞춘 구조)
    @GetMapping("/success")
    public ResponseEntity<Object> tossPaymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount
    ) {
        try {
            // Toss Payments 승인 처리
            PaymentSuccessDto result = paymentService.processPaymentSuccess(paymentKey, orderId, amount);
            return ResponseEntity.ok().body(result);  // 결과를 바로 반환
        } catch (Exception e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 승인 실패: " + e.getMessage());
        }
    }

    @GetMapping("/fail")
    public ResponseEntity<String> handleFailGet(@RequestParam String message, @RequestParam String code) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 실패: " + message + " (code: " + code + ")");
    }
}
