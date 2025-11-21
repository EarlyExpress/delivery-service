package com.early_express.delivery_service.delivery.application.service;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;
import com.early_express.delivery_service.delivery.infrastructure.FinalMileDeliveryRepository;
import com.early_express.delivery_service.delivery.presentation.rest.dto.DeliveryResponseForPagination;
import com.early_express.delivery_service.delivery.presentation.rest.dto.DeliveryStatusUpdateRequest;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryDetailResponse;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryRequest;
import com.early_express.delivery_service.global.common.utils.PageUtils;
import com.early_express.delivery_service.global.presentation.dto.PageResponse;
import com.early_express.delivery_service.global.presentation.exception.DeliveryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinalMileDeliveryService {
    private final FinalMileDeliveryRepository finalMileDeliveryRepository;

    /**
     * 배송 등록
     * DTO로부터 엔티티 생성 및 초기 상태 설정
     * "배송 담당자가 상품을 인수했다 (PICKED_UP)"는 의미
     *
     * @param agentId
     * @param req
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String registerDelivery(String agentId, FinalMileDeliveryRequest req) {

        LocalDateTime now = LocalDateTime.now(); //현재 시간을 미리 정의

        FinalMileDelivery delivery = FinalMileDelivery.builder()
                .orderId(req.orderId())
                .agentId(agentId)
                .deliveryAddress(req.deliveryAddress())
                .recipientName(req.recipientName())
                .recipientSlackId(req.recipientSlackId())
                .expectedTime(req.expectedTime())
                .build();

        delivery.pickedUp(now);
        //finalMileDeliveryRepository.save(delivery); (수정 전)
        FinalMileDelivery savedDelivery = finalMileDeliveryRepository.save(delivery); //(수정 후)

        //이벤트 발행

        return savedDelivery.getFinalMileId();

    }

    public FinalMileDeliveryDetailResponse getDeliveryDetail(String finalMileId) {
        FinalMileDelivery delivery = finalMileDeliveryRepository.findById(finalMileId)
                .orElseThrow(() -> new DeliveryNotFoundException("FinalMileId: " + finalMileId + " 배송 정보를 찾을 수 없습니다."));

        return new FinalMileDeliveryDetailResponse(
                delivery.getFinalMileId(),
                delivery.getOrderId(),
                delivery.getAgentId(),
                delivery.getCurrentStatus(),
                delivery.getDeliveryAddress(),
                delivery.getRecipientName(),
                delivery.getRecipientSlackId(),
                delivery.getStartedAt(),
                delivery.getExpectedTime(),
                delivery.getDeliveredAt()
        );
    }

    /**
     * 배송 상태를 변경합니다.
     * 엔티티의 도메인 메소드 (예: delivered(), onDelivery())를 호출하여 비즈니스 규칙을 적용합니다.
     * * @param finalMileId 상태를 변경할 배송 건 ID
     * @param req 변경할 새로운 상태 정보
     */
    @Transactional
    public void updateDeliveryStatus(String finalMileId, DeliveryStatusUpdateRequest req) {

        // 1. 엔티티 조회 (영속성 컨텍스트에 로드)
        FinalMileDelivery delivery = finalMileDeliveryRepository.findById(finalMileId)
                .orElseThrow(() -> new DeliveryNotFoundException(
                        "FinalMileId: " + finalMileId + " 에 해당하는 배송 정보를 찾을 수 없습니다.")
                );

        // 2. 요청된 상태에 따라 해당 도메인 메소드 호출
        FinalMileDeliveryStatus newStatus = req.newStatus();

        switch (newStatus) {
            case ON_THE_WAY:
                // ON_THE_WAY은 픽업 이후에만 가능하므로 엔티티 내부에서 검증됩니다.
                delivery.onDelivery();
                break;
            case DELIVERED:
                // 배송 완료 처리. delivered() 메소드가 deliveredAt을 현재 시간으로 설정합니다.
                delivery.delivered();
                // 💡 (TODO: 이벤트 발행) publisher.publishEvent(new DeliveryCompletedEvent(finalMileId));
                break;
            case FAILED:
                // 배송 실패 처리 (수령인 부재 등)
                delivery.deliveryFailed();
                break;
            case CANCELED:
                // 배송 취소 처리
                delivery.deliveryCancelled();
                break;
            case PICKED_UP:
                // 이미 PICKED_UP 상태로 등록되었으므로,
                // 재차 이 상태로 변경하려면 별도의 비즈니스 로직이 필요하거나, 무시합니다.
                if (delivery.getCurrentStatus() != FinalMileDeliveryStatus.PICKED_UP) {
                    delivery.pickedUp(LocalDateTime.now());
                }
                break;
            default:
                throw new IllegalStateException("지원하지 않는 상태 변경 요청입니다: " + newStatus);
        }

        // 3. 트랜잭션 종료 시, Dirty Checking(변경 감지)에 의해 DB에 자동으로 반영됩니다.
    }

    /**
     * @param deletedBy 삭제를 요청한 주체 (예: Agent ID, System ID)
     */
    @Transactional
    public void softDeleteDelivery(String finalMileId, String deletedBy) {

        FinalMileDelivery delivery = finalMileDeliveryRepository.findById(finalMileId)
                .orElseThrow(() -> new DeliveryNotFoundException("FinalMileId: " + finalMileId + " 배송 정보를 찾을 수 없습니다."));

        // 💡 1. 엔티티의 Soft Delete 도메인 메소드 호출
        delivery.markForSoftDeletion(deletedBy);

        // 2. 트랜잭션 종료 시, JPA가 변경된 isDeleted, deletedAt, deletedBy 필드를 DB에 반영합니다.
    }
}
