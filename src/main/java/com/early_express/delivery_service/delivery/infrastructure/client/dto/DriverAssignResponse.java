package com.early_express.delivery_service.delivery.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 드라이버 배정 응답
 * Last Mile Driver Service → Delivery Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignResponse {

    private String driverId;
    private String userId;
    private String hubId;
    private String driverName;
    private String status;
    private LocalDateTime assignedAt;

    /**
     * 배정 성공 여부
     */
    public boolean isSuccess() {
        return driverId != null && !driverId.isBlank();
    }
}