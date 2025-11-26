package com.early_express.delivery_service.delivery.presentation.internal;

import com.early_express.delivery_service.delivery.application.service.FinalMileDeliveryService;
import com.early_express.delivery_service.delivery.presentation.internal.dto.request.LastMileCreateRequest;
import com.early_express.delivery_service.delivery.presentation.internal.dto.response.LastMileAssignDriverResponse;
import com.early_express.delivery_service.delivery.presentation.internal.dto.response.LastMileCancelResponse;
import com.early_express.delivery_service.delivery.presentation.internal.dto.response.LastMileCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 최종 배송 Internal Controller
 * 내부 서비스 간 통신용 (Order Service, Track Service)
 */
@Slf4j
@RestController
@RequestMapping("/v1/last-mile/internal")
@RequiredArgsConstructor
public class LastMileInternalController {

    private final FinalMileDeliveryService finalMileDeliveryService;

    /**
     * 최종 배송 생성
     * POST /v1/last-mile/internal/deliveries
     *
     * Order Service에서 주문 생성 시 호출
     * - 담당자 미배정 상태 (PENDING)
     */
    @PostMapping("/deliveries")
    public ResponseEntity<LastMileCreateResponse> createDelivery(
            @Valid @RequestBody LastMileCreateRequest request) {

        log.info("[Internal] 최종 배송 생성 요청 - orderId: {}, hubId: {}",
                request.getOrderId(), request.getHubId());

        LastMileCreateResponse response = finalMileDeliveryService.createDelivery(request);

        log.info("[Internal] 최종 배송 생성 완료 - finalMileId: {}, orderId: {}",
                response.getLastMileDeliveryId(), response.getOrderId());

        return ResponseEntity.ok(response);
    }

    /**
     * 담당자 배정
     * POST /v1/last-mile/internal/deliveries/{lastMileDeliveryId}/assign-driver
     *
     * Track Service에서 호출
     * - LastMileDriverService에 Feign 요청 → 드라이버 배정
     * - 픽업 + 출발 처리
     * - LastMileDepartedEvent 발행
     */
    @PostMapping("/deliveries/{lastMileDeliveryId}/assign-driver")
    public ResponseEntity<LastMileAssignDriverResponse> assignDriver(
            @PathVariable String lastMileDeliveryId) {

        log.info("[Internal] 담당자 배정 요청 - lastMileDeliveryId: {}", lastMileDeliveryId);

        LastMileAssignDriverResponse response = finalMileDeliveryService.assignDriver(lastMileDeliveryId);

        log.info("[Internal] 담당자 배정 완료 - lastMileDeliveryId: {}, driverId: {}, driverName: {}",
                lastMileDeliveryId, response.getDriverId(), response.getDriverName());

        return ResponseEntity.ok(response);
    }

    /**
     * 배송 취소
     * POST /v1/last-mile/internal/deliveries/{lastMileDeliveryId}/cancel
     *
     * Order Service 또는 Track Service에서 호출 (보상 트랜잭션)
     */
    @PostMapping("/deliveries/{lastMileDeliveryId}/cancel")
    public ResponseEntity<LastMileCancelResponse> cancelDelivery(
            @PathVariable String lastMileDeliveryId) {

        log.info("[Internal] 배송 취소 요청 - lastMileDeliveryId: {}", lastMileDeliveryId);

        finalMileDeliveryService.cancelDelivery(lastMileDeliveryId);

        log.info("[Internal] 배송 취소 완료 - lastMileDeliveryId: {}", lastMileDeliveryId);

        return ResponseEntity.ok(LastMileCancelResponse.success(lastMileDeliveryId));
    }
}