package com.early_express.delivery_service.delivery.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 드라이버 작업 응답
 * Last Mile Driver Service → Delivery Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverOperationResponse {

    private String driverId;
    private String status;
    private String message;
    private Long timestamp;

    /**
     * 작업 성공 여부
     */
    public boolean isSuccess() {
        return driverId != null && !driverId.isBlank();
    }
}