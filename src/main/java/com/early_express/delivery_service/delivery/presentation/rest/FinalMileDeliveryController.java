package com.early_express.delivery_service.delivery.presentation.rest;

import com.early_express.delivery_service.delivery.application.service.FinalMileDeliveryService;
import com.early_express.delivery_service.delivery.presentation.rest.dto.DeliveryStatusUpdateRequest;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryDetailResponse;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryRequest;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryResponse;
import com.early_express.delivery_service.global.presentation.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="업체배송 API", description = "배송 신규 생성 등의 기능을 위한 API")
@RestController
@RequestMapping("/api/v1/last-mile")
@RequiredArgsConstructor
public class FinalMileDeliveryController {
    private final FinalMileDeliveryService finalMileDeliveryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // HTTP 201 Created 반환
    public FinalMileDeliveryResponse registerDelivery(
            @Valid @RequestBody FinalMileDeliveryRequest req,
            //@AuthenticationPrincipal UserDetailsImpl userDetails (인증 구현 후 적용)
            @RequestHeader("X-Agent-Id") String agentId) {
        // 1. Service 계층 호출 및 배송 ID (String) 반환
        String finalMileId = finalMileDeliveryService.registerDelivery(agentId, req);

        // 2. 응답 DTO 생성 및 반환
//        return ApiResponse<FinalMileDeliveryDetailResponse>
        return new FinalMileDeliveryResponse(finalMileId);
    }

    @GetMapping("/{finalMileId}")
    @ResponseStatus(HttpStatus.OK)
    public FinalMileDeliveryDetailResponse getDeliveryDetail(@PathVariable String finalMileId) {
        FinalMileDeliveryDetailResponse response =
                finalMileDeliveryService.getDeliveryDetail(finalMileId);

        return response;
    }

    /**
     * PATCH /api/v1/last-mile/{finalMileId}
     * 특정 배송 건의 상태를 변경합니다.
     */
    @PatchMapping("/{finalMileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // HTTP 204 No Content 반환 (성공했으나 본문 없음)
    public void updateDeliveryStatus(
            @PathVariable String finalMileId,
            @Valid @RequestBody DeliveryStatusUpdateRequest req
            //@AuthenticationPrincipal UserDetailsImpl userDetails (인증 구현 후 적용)
    ) {

        // 1. Service 계층 호출
        finalMileDeliveryService.updateDeliveryStatus(finalMileId, req);
    }

    @DeleteMapping("/{finalMileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void softDeleteDelivery(
            @PathVariable String finalMileId,
            @RequestHeader("X-Agent-Id") String deletedBy // 💡 헤더에서 삭제자 ID를 받습니다.
    ) {
        // Service 호출
        finalMileDeliveryService.softDeleteDelivery(finalMileId, deletedBy);
    }
}
