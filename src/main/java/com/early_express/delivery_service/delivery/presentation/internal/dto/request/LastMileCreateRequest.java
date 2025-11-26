package com.early_express.delivery_service.delivery.presentation.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 최종 배송 생성 요청 (Internal)
 * Order Service → Delivery Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMileCreateRequest {

    /**
     * 주문 ID (추적용)
     */
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    /**
     * 도착 허브 ID (배송 출발 허브)
     */
    @NotBlank(message = "허브 ID는 필수입니다.")
    private String hubId;

    /**
     * 배송 주소
     */
    @NotBlank(message = "배송 주소는 필수입니다.")
    private String deliveryAddress;

    /**
     * 수령인 이름
     */
    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String recipientName;

    /**
     * 수령인 Slack ID
     */
    private String recipientSlackId;

    /**
     * 예상 도착 시간
     */
    private LocalDateTime expectedTime;
}