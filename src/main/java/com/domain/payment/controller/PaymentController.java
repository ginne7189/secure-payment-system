package com.domain.payment.controller;

import com.domain.payment.dto.PaymentSuccessDto;
import com.domain.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST 요청 처리
    @PostMapping("/success")
    public ResponseEntity<String> handleSuccessPost(@RequestParam String paymentKey,
                                                    @RequestParam String orderId,
                                                    @RequestParam Long amount) {
        try {
            // 결제 승인 요청 및 처리
            PaymentSuccessDto result = paymentService.tossPaymentSuccess(paymentKey, orderId, amount);
            return ResponseEntity.ok("결제 승인 성공: " + result);
        } catch (Exception e) {
            // 예외 발생 시 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 승인 실패: " + e.getMessage());
        }
    }

    // GET 요청 처리 추가
    @GetMapping("/success")
    public ResponseEntity<String> handleSuccessGet(@RequestParam String paymentKey,
                                                   @RequestParam String orderId,
                                                   @RequestParam Long amount) {
        return handleSuccessPost(paymentKey, orderId, amount);
    }

    @PostMapping("/fail")
    public ResponseEntity<String> handleFailPost(@RequestParam String message, @RequestParam String code) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 실패: " + message + " (code: " + code + ")");
    }

    // GET 요청 처리 추가
    @GetMapping("/fail")
    public ResponseEntity<String> handleFailGet(@RequestParam String message, @RequestParam String code) {
        return handleFailPost(message, code);
    }
}
