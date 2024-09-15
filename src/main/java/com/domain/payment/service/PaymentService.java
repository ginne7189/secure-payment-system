package com.domain.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.api-url}")
    private String tossApiUrl;

    public PaymentService(RestTemplate restTemplate, WebClient.Builder webClientBuilder) {
        this.restTemplate = restTemplate;
        this.webClient = webClientBuilder.baseUrl(tossApiUrl).build();
    }

    // 결제 요청
    public String requestPayment(int amount, String orderId, String orderName, String customerEmail, String customerName) {
        String url = tossApiUrl;

        HttpHeaders headers = new HttpHeaders();
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderName", orderName);
        body.put("customerEmail", customerEmail);
        body.put("customerName", customerName);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(url, request, String.class);
        } catch (HttpClientErrorException e) {
            return "Error occurred: " + e.getResponseBodyAsString();
        } catch (RestClientException e) {
            return "An error occurred while processing the request.";
        }
    }

    // 결제 승인(확인)
    public String confirmPayment(String paymentKey, String orderId, int amount) {
        String url = tossApiUrl + "/confirm";

        HttpHeaders headers = new HttpHeaders();
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(url, request, String.class);
        } catch (HttpClientErrorException e) {
            return "Error occurred: " + e.getResponseBodyAsString();
        } catch (RestClientException e) {
            return "An error occurred while processing the request.";
        }
    }

    // 지난 30일간 결제 데이터 가져오기
    public List<PaymentData> getPaymentsForLast30Days() {
        return getPaymentHistory(30);
    }

    // 지난 7일간 결제 데이터 가져오기
    public List<PaymentData> getPaymentsForLast7Days() {
        return getPaymentHistory(7);
    }

    private List<PaymentData> getPaymentHistory(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        String url = String.format("/v1/payments?start_date=%s&end_date=%s", startDate, endDate);

        return webClient.get()
                .uri(url)
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)))
                .retrieve()
                .bodyToFlux(PaymentData.class)
                .collectList()
                .block();
    }
}
