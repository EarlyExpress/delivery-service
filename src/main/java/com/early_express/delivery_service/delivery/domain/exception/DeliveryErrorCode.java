package com.early_express.delivery_service.delivery.domain.exception;

import com.early_express.delivery_service.global.presentation.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Delivery 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    // === 조회 관련 (404) ===
    DELIVERY_NOT_FOUND("DELIVERY_001", "배송 정보를 찾을 수 없습니다.", 404),
    DRIVER_NOT_FOUND("DELIVERY_002", "배송 담당자를 찾을 수 없습니다.", 404),

    // === 상태 관련 (400) ===
    INVALID_DELIVERY_STATUS("DELIVERY_100", "유효하지 않은 배송 상태입니다.", 400),
    INVALID_STATUS_TRANSITION("DELIVERY_101", "유효하지 않은 상태 전이입니다.", 400),
    DELIVERY_ALREADY_COMPLETED("DELIVERY_102", "이미 완료된 배송입니다.", 400),
    DELIVERY_ALREADY_CANCELED("DELIVERY_103", "이미 취소된 배송입니다.", 400),

    // === 담당자 배정 관련 (400) ===
    DRIVER_ALREADY_ASSIGNED("DELIVERY_110", "이미 담당자가 배정되어 있습니다.", 400),
    DRIVER_ASSIGN_FAILED("DELIVERY_111", "담당자 배정에 실패했습니다.", 400),
    NO_AVAILABLE_DRIVER("DELIVERY_112", "배정 가능한 담당자가 없습니다.", 400),

    // === 데이터 검증 관련 (400) ===
    INVALID_ORDER_ID("DELIVERY_200", "유효하지 않은 주문 ID입니다.", 400),
    INVALID_HUB_ID("DELIVERY_201", "유효하지 않은 허브 ID입니다.", 400),
    INVALID_ADDRESS("DELIVERY_202", "유효하지 않은 주소입니다.", 400),

    // === 중복 관련 (409) ===
    DELIVERY_ALREADY_EXISTS("DELIVERY_300", "이미 해당 주문의 배송 정보가 존재합니다.", 409),

    // === 외부 서비스 연결 (5xx) ===
    EXTERNAL_SERVICE_ERROR("DELIVERY_501", "외부 서비스 오류가 발생했습니다.", 500),
    EXTERNAL_SERVICE_UNAVAILABLE("DELIVERY_502", "외부 서비스를 사용할 수 없습니다.", 503);

    private final String code;
    private final String message;
    private final int status;
}
