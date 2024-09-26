package com.domain.payment.service;

import com.domain.payment.dto.PaymentSuccessDto;
import com.domain.payment.entity.Payment;
import com.domain.payment.exception.CustomLogicException;
import com.domain.payment.exception.ExceptionCode;
import com.domain.payment.repository.PaymentRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.api-url}")
    private String tossApiUrl;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // 결제 성공 처리 로직 (JSON 사용)
    @Transactional
    public PaymentSuccessDto processPaymentSuccess(String paymentKey, String orderId, Long amount) {
        // 결제 정보 검증 및 DB 조회
        Payment payment = verifyPayment(orderId, amount);

        // 결제 승인 요청 (Toss API 호출)
        PaymentSuccessDto result = requestPaymentApproval(paymentKey, orderId, amount);

        // 결제 상태 업데이트
        payment.setPaymentKey(paymentKey);
        payment.setPaySuccessYN(true);
        payment.setApprovedAt(LocalDateTime.now()); // 승인 시간 추가
        paymentRepository.save(payment);  // 업데이트된 결제 정보를 저장

        return result;
    }

    // 결제 정보 검증 (DB에서 조회)
    private Payment verifyPayment(String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.PAYMENT_NOT_FOUND));

        if (!payment.getAmount().equals(amount)) {
            throw new CustomLogicException(ExceptionCode.PAYMENT_AMOUNT_EXP);
        }
        return payment;
    }

    // 결제 승인 요청 (JSON으로 처리)
    @Transactional
    public PaymentSuccessDto requestPaymentApproval(String paymentKey, String orderId, Long amount) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();

        // JSON 데이터로 결제 정보 구성
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("orderId", orderId);
        jsonRequest.put("amount", amount);

        PaymentSuccessDto result;
        try {
            result = restTemplate.postForObject(
                    tossApiUrl + "/v1/payments/confirm/" + paymentKey,
                    new HttpEntity<>(jsonRequest.toString(), headers),
                    PaymentSuccessDto.class
            );
        } catch (Exception e) {
            throw new CustomLogicException(ExceptionCode.ALREADY_APPROVED);
        }

        return result;
    }

    // Toss API 호출 시 필요한 헤더 설정
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encodedAuthKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.setBasicAuth(encodedAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
