package com.early_express.delivery_service.delivery.presentation.internal.dto;

import com.early_express.delivery_service.delivery.application.service.dto.AgentDeliveryGroupDto;
import com.early_express.delivery_service.delivery.application.service.dto.TodayDeliveryGroupResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 당일 담당자별 배송 그룹화 Internal 응답 DTO
 */
@Getter
@Builder
public class TodayDeliveryGroupInternalResponse {

    private final boolean success;
    private final String message;
    private final LocalDate targetDate;
    private final int totalAgents;
    private final int totalDeliveries;
    private final List<AgentDeliveryGroupDto> agentGroups;

    public static TodayDeliveryGroupInternalResponse from(TodayDeliveryGroupResponse response) {
        return TodayDeliveryGroupInternalResponse.builder()
                .success(true)
                .message("당일 배송 목록 조회 성공")
                .targetDate(response.getTargetDate())
                .totalAgents(response.getTotalAgents())
                .totalDeliveries(response.getTotalDeliveries())
                .agentGroups(response.getAgentGroups())
                .build();
    }

    public static TodayDeliveryGroupInternalResponse empty(LocalDate date) {
        return TodayDeliveryGroupInternalResponse.builder()
                .success(true)
                .message("당일 배송 건이 없습니다")
                .targetDate(date)
                .totalAgents(0)
                .totalDeliveries(0)
                .agentGroups(List.of())
                .build();
    }
}
