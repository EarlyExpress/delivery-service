package com.early_express.delivery_service.delivery.domain;

// Enum은 별도의 의존성 없이 자바 표준으로 사용 가능합니다.
public enum FinalMileDeliveryStatus {

    // 🚚 최종 배송 단계의 핵심 상태값

    /**
     * 최종 배송 담당자(Agent)가 물품을 인수하여 배송을 시작할 준비가 된 상태
     */
    PICKED_UP("인수 완료"),

    /**
     * 담당자가 최종 배송지로 이동 중인 상태 (고객에게 곧 도착 예정)
     */
    ON_THE_WAY("배송 중"),

    /**
     * 물품이 수령인에게 성공적으로 전달된 상태
     */
    DELIVERED("배송 완료"),

    /**
     * 배송 실패 (수취인 부재, 수취 거부, 주소 오류 등)
     */
    FAILED("배송 실패"),

    /**
     * 배송이 실패하여 물류센터로 돌아가거나 재배송 대기 중인 상태 (추가)
     */
    CANCELED("배송 취소/반품");

    private final String description;

    FinalMileDeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
