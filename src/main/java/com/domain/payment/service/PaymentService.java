package com.domain.payment.service;

import com.domain.payment.dto.PaymentRequestDto;
import com.domain.payment.dto.PaymentSuccessDto;
import com.domain.payment.entity.Customer;
import com.domain.payment.entity.Payment;
import com.domain.payment.exception.CustomLogicException;
import com.domain.payment.exception.ExceptionCode;
import com.domain.payment.repository.CustomerRepository;
import com.domain.payment.repository.PaymentRepository;
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
    private final CustomerRepository customerRepository;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.api-url}")
    private String tossApiUrl;

    public PaymentService(PaymentRepository paymentRepository, CustomerRepository customerRepository) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
    }

    // 결제 요청 시 Payment 엔티티를 먼저 저장
    @Transactional
    public void savePayment(String orderId, Long amount, String customerId) {
        Customer customer = customerRepository.findById(Long.parseLong(customerId))
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.CUSTOMER_NOT_FOUND));

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setCustomer(customer);  // Customer 설정
        payment.setPaySuccessYN(false); // 결제 성공 여부 초기값: false
        payment.setRequestedAt(LocalDateTime.now()); // 요청 시간 추가

        System.out.println("Saving payment: " + payment.getOrderId());
        paymentRepository.save(payment); // 결제 정보를 DB에 저장
        System.out.println("Payment saved: " + payment.getOrderId());
    }

    // 결제 성공 처리 로직
    @Transactional
    public PaymentSuccessDto tossPaymentSuccess(String paymentKey, String orderId, Long amount) {
        // 결제 정보 검증 및 DB 조회
        Payment payment = verifyPayment(orderId, amount);

        // 결제 승인 요청
        PaymentSuccessDto result = requestPaymentAccept(paymentKey, orderId, amount);

        // 결제 상태 업데이트
        payment.setPaymentKey(paymentKey);
        payment.setPaySuccessYN(true);
        payment.setApprovedAt(LocalDateTime.now()); // 승인 시간 추가

        // 결제 정보를 DB에 저장 (업데이트 처리)
        paymentRepository.save(payment);

        // 고객의 포인트 업데이트 로직 (예시)
        payment.getCustomer().setPoint((int) (payment.getCustomer().getPoint() + amount));

        return result;
    }

    // 결제 정보 검증 (DB에서 조회)
    public Payment verifyPayment(String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> {
            throw new CustomLogicException(ExceptionCode.PAYMENT_NOT_FOUND);
        });

        if (!payment.getAmount().equals(amount)) {
            throw new CustomLogicException(ExceptionCode.PAYMENT_AMOUNT_EXP);
        }

        return payment;
    }

    // 결제 승인 요청
    @Transactional
    public PaymentSuccessDto requestPaymentAccept(String paymentKey, String orderId, Long amount) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();

        PaymentRequestDto params = new PaymentRequestDto(orderId, amount);

        PaymentSuccessDto result;
        try {
            result = restTemplate.postForObject(
                    tossApiUrl + "/v1/payments/confirm/" + paymentKey,
                    new HttpEntity<>(params, headers),
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
