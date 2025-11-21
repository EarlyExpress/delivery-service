package com.early_express.delivery_service.delivery.presentation.rest.dto;

import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;

public record DeliveryResponseForPagination(
        String finalMileId,
        String orderId,
        String agentId,
        FinalMileDeliveryStatus currentStatus,
        String deliveryAddress,
        String recipientName,
        String recipientSlackId
) {
}
