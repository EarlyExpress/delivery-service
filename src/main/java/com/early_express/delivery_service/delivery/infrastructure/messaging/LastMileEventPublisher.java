package com.early_express.delivery_service.delivery.infrastructure.messaging;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.infrastructure.messaging.event.LastMileCompletedEvent;
import com.early_express.delivery_service.delivery.infrastructure.messaging.event.LastMileDepartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Last Mile 이벤트 발행자
 * Delivery Service → Track Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LastMileEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.last-mile-departed:last-mile-departed}")
    private String departedTopic;

    @Value("${spring.kafka.topic.last-mile-completed:last-mile-completed}")
    private String completedTopic;

    /**
     * 최종 배송 출발 이벤트 발행
     */
    public void publishDepartedEvent(FinalMileDelivery delivery) {
        LastMileDepartedEvent event = LastMileDepartedEvent.from(delivery);

        log.info("LastMileDepartedEvent 발행 - topic: {}, orderId: {}, finalMileId: {}",
                departedTopic, event.getOrderId(), event.getLastMileDeliveryId());

        kafkaTemplate.send(departedTopic, delivery.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("LastMileDepartedEvent 발행 실패 - orderId: {}, error: {}",
                                event.getOrderId(), ex.getMessage(), ex);
                    } else {
                        log.info("LastMileDepartedEvent 발행 성공 - orderId: {}, partition: {}, offset: {}",
                                event.getOrderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * 최종 배송 완료 이벤트 발행
     */
    public void publishCompletedEvent(FinalMileDelivery delivery) {
        LastMileCompletedEvent event = LastMileCompletedEvent.from(delivery);

        log.info("LastMileCompletedEvent 발행 - topic: {}, orderId: {}, finalMileId: {}",
                completedTopic, event.getOrderId(), event.getLastMileDeliveryId());

        kafkaTemplate.send(completedTopic, delivery.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("LastMileCompletedEvent 발행 실패 - orderId: {}, error: {}",
                                event.getOrderId(), ex.getMessage(), ex);
                    } else {
                        log.info("LastMileCompletedEvent 발행 성공 - orderId: {}, partition: {}, offset: {}",
                                event.getOrderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}