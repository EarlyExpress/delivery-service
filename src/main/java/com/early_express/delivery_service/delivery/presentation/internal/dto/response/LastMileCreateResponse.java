package com.early_express.delivery_service.delivery.presentation.internal.dto.response;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 최종 배송 생성 응답 (Internal)
 * Delivery Service → Order Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMileCreateResponse {

    /**
     * 생성된 배송 ID
     */
    private String lastMileDeliveryId;

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 허브 ID
     */
    private String hubId;

    /**
     * 배송 상태
     */
    private String status;

    /**
     * 메시지
     */
    private String message;

    /**
     * Entity → Response 변환
     */
    public static LastMileCreateResponse from(FinalMileDelivery delivery) {
        return LastMileCreateResponse.builder()
                .lastMileDeliveryId(delivery.getFinalMileId())
                .orderId(delivery.getOrderId())
                .hubId(delivery.getHubId())
                .status(delivery.getCurrentStatus().name())
                .message("최종 배송이 생성되었습니다.")
                .build();
    }

    /**
     * 성공 여부
     */
    public boolean isSuccess() {
        return lastMileDeliveryId != null && !lastMileDeliveryId.isBlank();
    }
}