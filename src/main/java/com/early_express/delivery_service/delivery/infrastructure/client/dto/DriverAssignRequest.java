package com.early_express.delivery_service.delivery.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 드라이버 배정 요청
 * Delivery Service → Last Mile Driver Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignRequest {

    private String hubId;
    private String deliveryId;

    public static DriverAssignRequest of(String hubId, String deliveryId) {
        return DriverAssignRequest.builder()
                .hubId(hubId)
                .deliveryId(deliveryId)
                .build();
    }
}