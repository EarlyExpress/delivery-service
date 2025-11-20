package com.early_express.delivery_service.delivery.presentation.rest.dto;

import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusUpdateRequest(
        @NotNull(message = "변경할 상태는 필수입니다.")
        FinalMileDeliveryStatus newStatus
) {}
