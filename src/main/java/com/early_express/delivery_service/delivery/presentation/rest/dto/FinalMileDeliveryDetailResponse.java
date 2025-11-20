package com.early_express.delivery_service.delivery.presentation.rest.dto;

import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;

import java.time.LocalDateTime;

public record FinalMileDeliveryDetailResponse(
        String finalMileId,
        String orderId,
        String agentId,
        FinalMileDeliveryStatus currentStatus,
        String deliveryAddress,
        String recipientName,
        String recipientSlackId,
        LocalDateTime startedAt,
        LocalDateTime expectedTime,
        LocalDateTime deliveredAt
) {
}
