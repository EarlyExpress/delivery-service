package com.early_express.delivery_service.delivery.domain;

/**
 * 최종 배송 상태
 */
public enum FinalMileDeliveryStatus {

    // ===== 초기 상태 =====

    /**
     * 배송 생성됨 (담당자 미배정)
     * - Order Service에서 생성 요청 시 초기 상태
     */
    PENDING("대기 중"),

    /**
     * 담당자 배정됨
     * - Track Service에서 드라이버 배정 요청 시
     */
    ASSIGNED("담당자 배정됨"),

    // ===== 배송 진행 상태 =====

    /**
     * 최종 배송 담당자(Agent)가 물품을 인수하여 배송을 시작할 준비가 된 상태
     */
    PICKED_UP("인수 완료"),

    /**
     * 담당자가 최종 배송지로 이동 중인 상태 (고객에게 곧 도착 예정)
     */
    ON_THE_WAY("배송 중"),

    // ===== 종료 상태 =====

    /**
     * 물품이 수령인에게 성공적으로 전달된 상태
     */
    DELIVERED("배송 완료"),

    /**
     * 배송 실패 (수취인 부재, 수취 거부, 주소 오류 등)
     */
    FAILED("배송 실패"),

    /**
     * 배송이 취소된 상태
     */
    CANCELED("배송 취소");

    private final String description;

    FinalMileDeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 담당자 배정 가능 여부
     */
    public boolean canAssignDriver() {
        return this == PENDING;
    }

    /**
     * 픽업 가능 여부
     */
    public boolean canPickUp() {
        return this == ASSIGNED;
    }

    /**
     * 출발 가능 여부
     */
    public boolean canDepart() {
        return this == PICKED_UP;
    }

    /**
     * 완료 가능 여부
     */
    public boolean canComplete() {
        return this == ON_THE_WAY || this == PICKED_UP;
    }

    /**
     * 취소 가능 여부
     */
    public boolean canCancel() {
        return this != DELIVERED && this != CANCELED;
    }

    /**
     * 종료 상태 여부
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED || this == CANCELED;
    }
}