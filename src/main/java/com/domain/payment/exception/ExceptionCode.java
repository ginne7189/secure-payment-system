package com.domain.payment.exception;

public enum ExceptionCode {
    CUSTOMER_NOT_FOUND("해당 고객을 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND("결제를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_EXP("결제 금액이 일치하지 않습니다."),
    ALREADY_APPROVED("이미 승인된 결제입니다."),
    UNAUTHORIZED_KEY("인증되지 않은 시크릿 키입니다.");

    private final String message;

    ExceptionCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
