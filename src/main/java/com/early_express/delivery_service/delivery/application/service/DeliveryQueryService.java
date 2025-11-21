package com.early_express.delivery_service.delivery.application.service;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.infrastructure.FinalMileDeliveryRepository;
import com.early_express.delivery_service.delivery.presentation.rest.dto.DeliveryResponseForPagination;
import com.early_express.delivery_service.global.common.utils.PageUtils;
import com.early_express.delivery_service.global.presentation.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Spring Bean으로 등록
@RequiredArgsConstructor
public class DeliveryQueryService {

    private final FinalMileDeliveryRepository finalMileDeliveryRepository;

    /**
     * FinalMileDelivery 엔티티를 FinalMileDeliveryDetailResponse DTO로 변환하는 매퍼 함수
     */
    private DeliveryResponseForPagination mapToDto(FinalMileDelivery delivery) {
        return new DeliveryResponseForPagination(
                delivery.getFinalMileId(),
                delivery.getOrderId(),
                delivery.getAgentId(),
                delivery.getCurrentStatus(),
                delivery.getDeliveryAddress(),
                delivery.getRecipientName(),
                delivery.getRecipientSlackId()
        );
    }

    /**
     * 페이지네이션 및 정렬이 적용된 배송 목록을 조회하고 PageResponse로 반환합니다.
     * @param pageable 클라이언트 요청(page, size, sort)을 담은 Spring Data Pageable 객체
     * @return DTO 목록과 PageInfo를 포함한 PageResponse
     */
    @Transactional(readOnly = true)
    public PageResponse<DeliveryResponseForPagination> getPaginatedDeliveries(Pageable pageable) {

        // 1. Repository 호출
        Page<FinalMileDelivery> deliveryPage = finalMileDeliveryRepository.findAll(pageable);

        // 2. PageUtils를 사용하여 Page<Entity>를 PageResponse<Pagination DTO>로 변환
        return PageUtils.toPageResponse(
                deliveryPage,
                this::mapToDto
        );
    }


}