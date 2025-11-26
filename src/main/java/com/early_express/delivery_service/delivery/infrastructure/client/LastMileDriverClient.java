package com.early_express.delivery_service.delivery.infrastructure.client;

import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverAssignRequest;
import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverAssignResponse;
import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverCompleteRequest;
import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverOperationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Last Mile Driver Service Feign Client
 * Delivery Service → Last Mile Driver Service
 */
@FeignClient(
        name = "last-mile-driver-service",
        url = "${client.last-mile-driver-service.url}",
        configuration = LastMileDriverClientConfig.class
)
public interface LastMileDriverClient {

    /**
     * 드라이버 자동 배정 (허브별)
     * - 해당 허브 소속 드라이버 중 우선순위가 가장 낮은 드라이버에게 배정
     *
     * @param request 배정 요청 (hubId, deliveryId)
     * @return 배정된 드라이버 정보
     */
    @PostMapping("/v1/last-mile-driver/internal/drivers/assign")
    DriverAssignResponse assignDriver(@RequestBody DriverAssignRequest request);

    /**
     * 배송 완료 통지
     * - 드라이버 상태를 AVAILABLE로 변경하고 통계 업데이트
     *
     * @param driverId 드라이버 ID
     * @param request 완료 정보 (배송 소요 시간 등)
     * @return 처리 결과
     */
    @PutMapping("/v1/last-mile-driver/internal/drivers/{driverId}/complete")
    DriverOperationResponse completeDelivery(
            @PathVariable("driverId") String driverId,
            @RequestBody DriverCompleteRequest request
    );

    /**
     * 배송 취소 통지
     * - 드라이버 배정 해제 및 상태를 AVAILABLE로 변경
     *
     * @param driverId 드라이버 ID
     * @return 처리 결과
     */
    @PutMapping("/v1/last-mile-driver/internal/drivers/{driverId}/cancel")
    DriverOperationResponse cancelDelivery(@PathVariable("driverId") String driverId);
}