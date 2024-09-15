package com.domain.payment.controller;

import com.domain.payment.service.PaymentData;
import com.domain.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 결제 요청
    @PostMapping("/request")
    public ResponseEntity<String> requestPayment(@RequestParam int amount,
                                                 @RequestParam String orderId,
                                                 @RequestParam String orderName,
                                                 @RequestParam String customerEmail,
                                                 @RequestParam String customerName) {
        String result = paymentService.requestPayment(amount, orderId, orderName, customerEmail, customerName);
        return ResponseEntity.ok(result);
    }

    // 결제 승인(확인)
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(@RequestParam String paymentKey,
                                                 @RequestParam String orderId,
                                                 @RequestParam int amount) {
        String result = paymentService.confirmPayment(paymentKey, orderId, amount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/30days")
    public ResponseEntity<List<PaymentData>> getPaymentsForLast30Days() {
        List<PaymentData> paymentDataList = paymentService.getPaymentsForLast30Days();
        return ResponseEntity.ok(paymentDataList);
    }

    @GetMapping("/history/7days")
    public ResponseEntity<List<PaymentData>> getPaymentsForLast7Days() {
        List<PaymentData> paymentDataList = paymentService.getPaymentsForLast7Days();
        return ResponseEntity.ok(paymentDataList);
    }
}
