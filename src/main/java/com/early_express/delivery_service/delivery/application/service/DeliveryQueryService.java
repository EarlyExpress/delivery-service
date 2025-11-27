package com.early_express.delivery_service.delivery.application.service;

import com.early_express.delivery_service.delivery.application.service.dto.AgentDeliveryGroupDto;
import com.early_express.delivery_service.delivery.application.service.dto.TodayDeliveryGroupResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 당일 배송 목록을 담당자(agentId)별로 그룹화하여 반환
     * @param hubId 허브 ID (null이면 전체 허브 대상)
     * @return 담당자별 그룹화된 배송 목록
     */
    @Transactional(readOnly = true)
    public TodayDeliveryGroupResponse getTodayDeliveriesGroupedByAgent(String hubId) {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 1. 당일 배송 목록 조회
        List<FinalMileDelivery> deliveries;
        if (hubId != null && !hubId.isBlank()) {
            deliveries = finalMileDeliveryRepository.findTodayAssignedDeliveriesByHub(hubId, startOfDay, endOfDay);
        } else {
            deliveries = finalMileDeliveryRepository.findTodayAssignedDeliveries(startOfDay, endOfDay);
        }

        // 2. agentId별 그룹화
        Map<String, List<FinalMileDelivery>> groupedByAgent = deliveries.stream()
                .collect(Collectors.groupingBy(FinalMileDelivery::getAgentId));

        // 3. DTO 변환
        List<AgentDeliveryGroupDto> agentGroups = groupedByAgent.entrySet().stream()
                .map(entry -> {
                    String agentId = entry.getKey();
                    List<FinalMileDelivery> agentDeliveries = entry.getValue();

                    // 첫 번째 배송에서 담당자 이름 추출 (동일 담당자이므로)
                    String agentName = agentDeliveries.isEmpty() ? null : agentDeliveries.get(0).getAgentName();

                    List<AgentDeliveryGroupDto.DeliveryItemDto> items = agentDeliveries.stream()
                            .map(this::mapToDeliveryItem)
                            .collect(Collectors.toList());

                    return AgentDeliveryGroupDto.builder()
                            .agentId(agentId)
                            .agentName(agentName)
                            .totalCount(items.size())
                            .deliveries(items)
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 최종 응답 생성
        return TodayDeliveryGroupResponse.builder()
                .targetDate(today)
                .totalAgents(agentGroups.size())
                .totalDeliveries(deliveries.size())
                .agentGroups(agentGroups)
                .build();
    }

    /**
     * FinalMileDelivery → DeliveryItemDto 변환
     */
    private AgentDeliveryGroupDto.DeliveryItemDto mapToDeliveryItem(FinalMileDelivery delivery) {
        return AgentDeliveryGroupDto.DeliveryItemDto.builder()
                .finalMileId(delivery.getFinalMileId())
                .orderId(delivery.getOrderId())
                .deliveryAddress(delivery.getDeliveryAddress())
                .recipientName(delivery.getRecipientName())
                .recipientSlackId(delivery.getRecipientSlackId())
                .currentStatus(delivery.getCurrentStatus().name())
                .expectedTime(delivery.getExpectedTime() != null
                        ? delivery.getExpectedTime().toString()
                        : null)
                .build();
    }
}