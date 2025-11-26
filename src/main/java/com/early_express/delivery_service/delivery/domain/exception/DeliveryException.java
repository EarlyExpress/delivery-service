package com.early_express.delivery_service.delivery.domain.exception;

import com.early_express.delivery_service.global.presentation.exception.GlobalException;

/**
 * Delivery 도메인 예외
 */
public class DeliveryException extends GlobalException {

    /**
     * ErrorCode만으로 예외 생성
     */
    public DeliveryException(DeliveryErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * ErrorCode와 사용자 정의 메시지로 예외 생성
     */
    public DeliveryException(DeliveryErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * ErrorCode와 원인 예외로 예외 생성
     */
    public DeliveryException(DeliveryErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * ErrorCode, 사용자 정의 메시지, 원인 예외로 예외 생성
     */
    public DeliveryException(DeliveryErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * ErrorCode 반환 (타입 캐스팅)
     */
    @Override
    public DeliveryErrorCode getErrorCode() {
        return (DeliveryErrorCode) super.getErrorCode();
    }
}