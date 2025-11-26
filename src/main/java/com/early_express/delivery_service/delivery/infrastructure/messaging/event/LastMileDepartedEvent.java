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
 * 최종 배송 출발 이벤트 (발행용)
 * Delivery Service → Track Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMileDepartedEvent {

    private String eventId;
    private String eventType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTime;

    private String orderId;
    private String lastMileDeliveryId;
    private String hubId;
    private String driverId;
    private String driverName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departedAt;

    /**
     * Entity → Event 변환
     */
    public static LastMileDepartedEvent from(FinalMileDelivery delivery) {
        return LastMileDepartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("LastMileDepartedEvent")
                .eventTime(LocalDateTime.now())
                .orderId(delivery.getOrderId())
                .lastMileDeliveryId(delivery.getFinalMileId())
                .hubId(delivery.getHubId())
                .driverId(delivery.getAgentId())
                .driverName(delivery.getAgentName())
                .departedAt(delivery.getDepartedAt())
                .build();
    }
}