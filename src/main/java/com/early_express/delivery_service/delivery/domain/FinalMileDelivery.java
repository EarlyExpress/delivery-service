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
    private String finalMileId;

    @Column(nullable = false)
    private String orderId;

    // [수정] nullable = false 제거 → 초기 생성 시 담당자 미배정 허용
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

    // ==================== [추가] 신규 필드 ====================

    /**
     * [추가] 출발 허브 ID
     * - 배송이 시작되는 허브
     */
    @Column(nullable = false)
    private String hubId;

    /**
     * [추가] 배송 담당자 이름
     * - 드라이버 배정 시 설정
     */
    private String agentName;

    /**
     * [추가] 배송 출발 시간
     * - ON_THE_WAY 상태 전환 시 설정
     */
    private LocalDateTime departedAt;

    // ==================== Builder ====================

    @Builder
    public FinalMileDelivery(String orderId, String hubId, String agentId, String agentName,
                             FinalMileDeliveryStatus currentStatus, String deliveryAddress,
                             String recipientName, String recipientSlackId, LocalDateTime startedAt,
                             LocalDateTime expectedTime) {
        this.orderId = orderId;
        this.hubId = hubId;
        this.agentId = agentId;
        this.agentName = agentName;
        this.currentStatus = currentStatus;
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.recipientSlackId = recipientSlackId;
        this.startedAt = startedAt;
        this.expectedTime = expectedTime;
    }

    @PrePersist
    public void generateId() {
        // DB에 저장되기 직전에 ID가 null인지 확인하고 생성
        if (this.finalMileId == null) {
            this.finalMileId = UuidUtils.generate();
        }
    }

    // ==================== 기존 비즈니스 메서드 ====================

    //Agent가 배송 상품 전달받음
    public void pickedUp(LocalDateTime startedAt) {

        if(this.currentStatus == FinalMileDeliveryStatus.DELIVERED ||
                this.currentStatus == FinalMileDeliveryStatus.CANCELED) {
            throw new IllegalStateException("이미 완료/취소된 배송입니다.");
        }

        this.currentStatus = FinalMileDeliveryStatus.PICKED_UP;

        if (this.startedAt == null) {
            this.startedAt = startedAt;
        }

    }

    //배송 중
    public void onDelivery() {
        if(this.currentStatus != FinalMileDeliveryStatus.PICKED_UP &&
                this.currentStatus != FinalMileDeliveryStatus.ON_THE_WAY) {
            throw new IllegalStateException("픽업되지 않은 상품은 배송 중 상태로 변경할 수 없습니다.");
        }

        this.currentStatus = FinalMileDeliveryStatus.ON_THE_WAY;

        // [추가] 출발 시간 기록
        if (this.departedAt == null) {
            this.departedAt = LocalDateTime.now();
        }
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

    public void markForSoftDeletion(String deletedBy) {

        // 1. 💡 도메인 규칙 검증
        if (this.currentStatus == FinalMileDeliveryStatus.DELIVERED) {
            throw new IllegalStateException("이미 완료된 배송(" + this.currentStatus + ")은 Soft Delete 처리할 수 없습니다.");
        }

        // 2. 🚀 BaseEntity의 Soft Delete 메소드를 호출하여 플래그 변경
        // 이 시점에서 isDeleted=true, deletedAt=now(), deletedBy=deletedBy가 설정됩니다.
        super.delete(deletedBy);
    }

    // ==================== [추가] 신규 팩토리 메서드 ====================

    /**
     * [추가] 최종 배송 생성 (Order Service에서 호출)
     * - 담당자 미배정 상태 (PENDING)
     *
     * @param orderId 주문 ID
     * @param hubId 출발 허브 ID
     * @param deliveryAddress 배송 주소
     * @param recipientName 수령인 이름
     * @param recipientSlackId 수령인 Slack ID
     * @param expectedTime 예상 도착 시간
     * @return FinalMileDelivery
     */
    public static FinalMileDelivery create(
            String orderId,
            String hubId,
            String deliveryAddress,
            String recipientName,
            String recipientSlackId,
            LocalDateTime expectedTime) {

        return FinalMileDelivery.builder()
                .orderId(orderId)
                .hubId(hubId)
                .agentId(null)      // 담당자 미배정
                .agentName(null)
                .currentStatus(FinalMileDeliveryStatus.PENDING)
                .deliveryAddress(deliveryAddress)
                .recipientName(recipientName)
                .recipientSlackId(recipientSlackId)
                .expectedTime(expectedTime)
                .build();
    }

    // ==================== [추가] 신규 비즈니스 메서드 ====================

    /**
     * [추가] 담당자 배정 (Track Service에서 호출)
     * PENDING → ASSIGNED
     *
     * @param agentId 담당자 ID
     * @param agentName 담당자 이름
     */
    public void assignAgent(String agentId, String agentName) {
        if (!this.currentStatus.canAssignDriver()) {
            throw new IllegalStateException(
                    "담당자 배정은 PENDING 상태에서만 가능합니다. 현재 상태: " + this.currentStatus);
        }

        this.agentId = agentId;
        this.agentName = agentName;
        this.currentStatus = FinalMileDeliveryStatus.ASSIGNED;
    }

    // ==================== [추가] 신규 조회 메서드 ====================

    /**
     * [추가] 담당자 배정 여부
     *
     * @return 담당자가 배정되어 있으면 true
     */
    public boolean hasAgent() {
        return this.agentId != null && !this.agentId.isBlank();
    }
}