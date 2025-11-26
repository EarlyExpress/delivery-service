package com.early_express.delivery_service.delivery.presentation.internal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배송 취소 응답 (Internal)
 * Delivery Service → Order Service / Track Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMileCancelResponse {

    /**
     * 배송 ID
     */
    private String lastMileDeliveryId;

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 메시지
     */
    private String message;

    /**
     * 성공 응답 생성
     */
    public static LastMileCancelResponse success(String lastMileDeliveryId) {
        return LastMileCancelResponse.builder()
                .lastMileDeliveryId(lastMileDeliveryId)
                .success(true)
                .message("배송이 취소되었습니다.")
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static LastMileCancelResponse failure(String lastMileDeliveryId, String message) {
        return LastMileCancelResponse.builder()
                .lastMileDeliveryId(lastMileDeliveryId)
                .success(false)
                .message(message)
                .build();
    }
}