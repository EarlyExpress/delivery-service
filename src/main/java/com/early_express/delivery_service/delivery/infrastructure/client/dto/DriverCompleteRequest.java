package com.early_express.delivery_service.delivery.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배송 완료 통지 요청
 * Delivery Service → Last Mile Driver Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverCompleteRequest {

    private Long deliveryTimeMin;

    public static DriverCompleteRequest of(Long deliveryTimeMin) {
        return DriverCompleteRequest.builder()
                .deliveryTimeMin(deliveryTimeMin)
                .build();
    }
}