package com.early_express.delivery_service.delivery.presentation.internal.dto.response;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverAssignResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 담당자 배정 응답 (Internal)
 * Delivery Service → Track Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMileAssignDriverResponse {

    /**
     * 배송 ID
     */
    private String lastMileDeliveryId;

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 배정된 드라이버 ID
     */
    private String driverId;

    /**
     * 배정된 드라이버 이름
     */
    private String driverName;

    /**
     * 배송 상태
     */
    private String status;

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 메시지
     */
    private String message;

    /**
     * 배정 시간
     */
    private LocalDateTime assignedAt;

    /**
     * Entity + DriverResponse → Response 변환
     */
    public static LastMileAssignDriverResponse from(
            FinalMileDelivery delivery,
            DriverAssignResponse driverResponse) {

        return LastMileAssignDriverResponse.builder()
                .lastMileDeliveryId(delivery.getFinalMileId())
                .orderId(delivery.getOrderId())
                .driverId(driverResponse.getDriverId())
                .driverName(driverResponse.getDriverName())
                .status(delivery.getCurrentStatus().name())
                .success(true)
                .message("담당자가 배정되었습니다.")
                .assignedAt(driverResponse.getAssignedAt())
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static LastMileAssignDriverResponse failure(String lastMileDeliveryId, String message) {
        return LastMileAssignDriverResponse.builder()
                .lastMileDeliveryId(lastMileDeliveryId)
                .success(false)
                .message(message)
                .build();
    }
}