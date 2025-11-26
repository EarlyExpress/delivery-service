package com.early_express.delivery_service.delivery.infrastructure.messaging.event;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 최종 배송 완료 이벤트 (발행용)
 * Delivery Service → Track Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMileCompletedEvent {

    private String eventId;
    private String eventType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTime;

    private String orderId;
    private String lastMileDeliveryId;
    private String hubId;
    private String driverId;
    private String driverName;
    private String recipientName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * Entity → Event 변환
     */
    public static LastMileCompletedEvent from(FinalMileDelivery delivery) {
        return LastMileCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("LastMileCompletedEvent")
                .eventTime(LocalDateTime.now())
                .orderId(delivery.getOrderId())
                .lastMileDeliveryId(delivery.getFinalMileId())
                .hubId(delivery.getHubId())
                .driverId(delivery.getAgentId())
                .driverName(delivery.getAgentName())
                .recipientName(delivery.getRecipientName())
                .completedAt(delivery.getDeliveredAt())
                .build();
    }
}