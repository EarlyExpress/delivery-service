package com.early_express.delivery_service.delivery.presentation.rest.dto;

import java.time.LocalDateTime;

public record FinalMileDeliveryRequest(
        String orderId,
        String deliveryAddress, // 최종 배송지 주소
        String recipientName,   // 수령인 이름
        String recipientSlackId, // 수령인에게 알림을 보낼 Slack ID
        LocalDateTime expectedTime
) {}
