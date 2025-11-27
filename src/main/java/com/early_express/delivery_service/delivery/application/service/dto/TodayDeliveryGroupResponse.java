package com.early_express.delivery_service.delivery.application.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 당일 배송 그룹화 응답 DTO
 */
@Getter
@Builder
public class TodayDeliveryGroupResponse {

    private final LocalDate targetDate;
    private final int totalAgents;
    private final int totalDeliveries;
    private final List<AgentDeliveryGroupDto> agentGroups;
}
