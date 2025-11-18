package com.early_express.delivery_service.delivery.domain;

import com.early_express.delivery_service.global.common.utils.UuidUtils;
import com.early_express.delivery_service.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "p_final_mile_delivery")
@Getter
@Access(AccessType.FIELD)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinalMileDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String finalMileId = UuidUtils.generate();

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String agentId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private FinalMileDeliveryStatus currentStatus;

    private String deliveryAddress;

    private String recipientName;

    private String recipientSlackId;

    private LocalDateTime startedAt;

    private LocalDateTime expectedTime;

    private LocalDateTime deliveredAt;

    @Builder
    public FinalMileDelivery(String orderId, String agentId, FinalMileDeliveryStatus currentStatus, String deliveryAddress,
                             String recipientName, String recipientSlackId, LocalDateTime startedAt,
                             LocalDateTime expectedTime) {
        this.orderId = orderId;
        this.agentId = agentId;
        this.currentStatus = currentStatus;
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.recipientSlackId = recipientSlackId;
        this.startedAt = startedAt;
        this.expectedTime = expectedTime;
    }


    //Agent가 배송 상품 전달받음
    public void pickedUp(LocalDateTime startedAt) {

        if(this.currentStatus == FinalMileDeliveryStatus.DELIVERED ||
            this.currentStatus == FinalMileDeliveryStatus.CANCELED) {
            throw new IllegalStateException("이미 완료/취소된 배송입니다.");
        }

        this.currentStatus = FinalMileDeliveryStatus.PICKED_UP;
        this.startedAt = startedAt;
    }

    //배송 중
    public void onDelivery() {
        if(this.currentStatus != FinalMileDeliveryStatus.PICKED_UP &&
            this.currentStatus != FinalMileDeliveryStatus.ON_THE_WAY) {
            throw new IllegalStateException("픽업되지 않은 상품은 배송 중 상태로 변경할 수 없습니다.");
        }

        this.currentStatus = FinalMileDeliveryStatus.ON_THE_WAY;
    }

    //배송 완료
    public void delivered() {
        if (this.currentStatus != FinalMileDeliveryStatus.ON_THE_WAY &&
                this.currentStatus != FinalMileDeliveryStatus.PICKED_UP) {
            throw new IllegalStateException("현재 상태(" + this.currentStatus + ")에서는 배송 완료 처리할 수 없습니다.");
        }

        this.currentStatus = FinalMileDeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    //배송 실패 (수령인 부재, 주소 오류 등)
    public void deliveryFailed() {
        // 완료된 배송은 실패 처리 불가
        if (this.currentStatus == FinalMileDeliveryStatus.DELIVERED) {
            throw new IllegalStateException("이미 완료된 배송을 실패 처리할 수 없습니다.");
        }
        this.currentStatus = FinalMileDeliveryStatus.FAILED;
        this.deliveredAt = LocalDateTime.now();
    }

    // 배송 취소 (영구적인 중단 및 반품/폐기 처리)
    public void deliveryCancelled() {
        if (this.currentStatus == FinalMileDeliveryStatus.DELIVERED) {
            throw new IllegalStateException("이미 완료된 배송은 취소할 수 없습니다. 반품 절차를 사용하세요.");
        }
        this.currentStatus = FinalMileDeliveryStatus.CANCELED;
    }
}
