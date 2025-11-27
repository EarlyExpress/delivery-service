package com.early_express.delivery_service.delivery.application.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 담당자별 배송 목록 그룹 DTO
 */
@Getter
@Builder
public class AgentDeliveryGroupDto {

    private final String agentId;
    private final String agentName;
    private final int totalCount;
    private final List<DeliveryItemDto> deliveries;

    @Getter
    @Builder
    public static class DeliveryItemDto {
        private final String finalMileId;
        private final String orderId;
        private final String deliveryAddress;
        private final String recipientName;
        private final String recipientSlackId;
        private final String currentStatus;
        private final String expectedTime;
    }
}
