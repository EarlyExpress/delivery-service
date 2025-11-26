package com.early_express.delivery_service.delivery.application.service;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;
import com.early_express.delivery_service.delivery.domain.exception.DeliveryErrorCode;
import com.early_express.delivery_service.delivery.domain.exception.DeliveryException;
import com.early_express.delivery_service.delivery.infrastructure.FinalMileDeliveryRepository;
import com.early_express.delivery_service.delivery.infrastructure.client.LastMileDriverClient;
import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverAssignRequest;
import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverAssignResponse;
import com.early_express.delivery_service.delivery.infrastructure.client.dto.DriverCompleteRequest;
import com.early_express.delivery_service.delivery.infrastructure.messaging.LastMileEventPublisher;
import com.early_express.delivery_service.delivery.presentation.internal.dto.request.LastMileCreateRequest;
import com.early_express.delivery_service.delivery.presentation.internal.dto.response.LastMileAssignDriverResponse;
import com.early_express.delivery_service.delivery.presentation.internal.dto.response.LastMileCreateResponse;
import com.early_express.delivery_service.delivery.presentation.rest.dto.DeliveryResponseForPagination;
import com.early_express.delivery_service.delivery.presentation.rest.dto.DeliveryStatusUpdateRequest;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryDetailResponse;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryRequest;
import com.early_express.delivery_service.global.common.utils.PageUtils;
import com.early_express.delivery_service.global.presentation.dto.PageResponse;
import com.early_express.delivery_service.global.presentation.exception.DeliveryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalMileDeliveryService {

    private final FinalMileDeliveryRepository finalMileDeliveryRepository;

    // [추가] Last Mile Driver Service 클라이언트
    private final LastMileDriverClient lastMileDriverClient;

    // [추가] 이벤트 발행기
    private final LastMileEventPublisher eventPublisher;

    // ==================== 기존 External API (배송 담당자 직접 호출) ====================

    /**
     * 배송 등록
     * DTO로부터 엔티티 생성 및 초기 상태 설정
     * "배송 담당자가 상품을 인수했다 (PICKED_UP)"는 의미
     *
//     * @param agentId
//     * @param req
     * @return
     */
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public String registerDelivery(String agentId, FinalMileDeliveryRequest req) {
//
//        LocalDateTime now = LocalDateTime.now(); //현재 시간을 미리 정의
//
//        FinalMileDelivery delivery = FinalMileDelivery.builder()
//                .orderId(req.orderId())
//                .hubId(req.hubId())  // [추가] hubId 필드
//                .agentId(agentId)
//                .agentName(req.agentName())  // [추가] agentName 필드
//                .deliveryAddress(req.deliveryAddress())
//                .recipientName(req.recipientName())
//                .recipientSlackId(req.recipientSlackId())
//                .expectedTime(req.expectedTime())
//                .currentStatus(FinalMileDeliveryStatus.PICKED_UP)  // [추가] 초기 상태 명시
//                .build();
//
//        delivery.pickedUp(now);
//        FinalMileDelivery savedDelivery = finalMileDeliveryRepository.save(delivery);
//
//        //이벤트 발행
//
//        return savedDelivery.getFinalMileId();
//
//    }

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

                // [추가] 드라이버에게 완료 통지
                notifyDriverCompletion(delivery);

                // [추가] LastMileCompletedEvent 발행
                eventPublisher.publishCompletedEvent(delivery);
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

    // ==================== [추가] Internal API (타 서비스 호출) ====================

    /**
     * [추가] 최종 배송 생성 (Order Service에서 호출)
     * - 담당자 미배정 상태 (PENDING)
     *
     * @param request 생성 요청 정보
     * @return 생성된 배송 정보
     */
    @Transactional
    public LastMileCreateResponse createDelivery(LastMileCreateRequest request) {
        log.info("최종 배송 생성 요청 - orderId: {}, hubId: {}",
                request.getOrderId(), request.getHubId());

        // 중복 체크
        if (finalMileDeliveryRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new DeliveryException(
                    DeliveryErrorCode.DELIVERY_ALREADY_EXISTS,
                    "주문 ID: " + request.getOrderId()
            );
        }

        // 배송 생성 (PENDING 상태, 담당자 미배정)
        FinalMileDelivery delivery = FinalMileDelivery.create(
                request.getOrderId(),
                request.getHubId(),
                request.getDeliveryAddress(),
                request.getRecipientName(),
                request.getRecipientSlackId(),
                request.getExpectedTime()
        );

        FinalMileDelivery savedDelivery = finalMileDeliveryRepository.save(delivery);

        log.info("최종 배송 생성 완료 - finalMileId: {}, orderId: {}, status: {}",
                savedDelivery.getFinalMileId(), savedDelivery.getOrderId(), savedDelivery.getCurrentStatus());

        return LastMileCreateResponse.from(savedDelivery);
    }

    /**
     * [추가] 배송 담당자 배정 (Track Service에서 호출)
     * - Last Mile Driver Service에 드라이버 배정 요청
     * - PENDING → ASSIGNED → PICKED_UP → ON_THE_WAY 자동 진행
     * - LastMileDepartedEvent 발행
     *
     * @param finalMileId 배송 ID
     * @return 배정 결과
     */
    @Transactional
    public LastMileAssignDriverResponse assignDriver(String finalMileId) {
        log.info("배송 담당자 배정 요청 - finalMileId: {}", finalMileId);

        // 배송 조회
        FinalMileDelivery delivery = finalMileDeliveryRepository.findById(finalMileId)
                .orElseThrow(() -> new DeliveryException(
                        DeliveryErrorCode.DELIVERY_NOT_FOUND,
                        "배송 ID: " + finalMileId
                ));

        // 이미 담당자가 배정되어 있는지 확인
        if (delivery.hasAgent()) {
            throw new DeliveryException(
                    DeliveryErrorCode.DRIVER_ALREADY_ASSIGNED,
                    "배송 ID: " + finalMileId + ", 현재 담당자: " + delivery.getAgentId()
            );
        }

        // Last Mile Driver Service에 드라이버 배정 요청
        DriverAssignResponse driverResponse;
        try {
            driverResponse = lastMileDriverClient.assignDriver(
                    DriverAssignRequest.of(delivery.getHubId(), finalMileId)
            );
            log.info("드라이버 배정 성공 - driverId: {}, driverName: {}",
                    driverResponse.getDriverId(), driverResponse.getDriverName());
        } catch (DeliveryException e) {
            throw e;  // 이미 DeliveryException이면 그대로 전파
        } catch (Exception e) {
            log.error("드라이버 배정 실패 - finalMileId: {}, error: {}", finalMileId, e.getMessage());
            throw new DeliveryException(
                    DeliveryErrorCode.DRIVER_ASSIGN_FAILED,
                    "드라이버 배정 중 오류 발생: " + e.getMessage(),
                    e
            );
        }

        // 담당자 배정 (PENDING → ASSIGNED)
        delivery.assignAgent(driverResponse.getDriverId(), driverResponse.getDriverName());

        // 픽업 처리 (ASSIGNED → PICKED_UP)
        delivery.pickedUp(LocalDateTime.now());

        // 출발 처리 (PICKED_UP → ON_THE_WAY)
        delivery.onDelivery();

        // 저장
        finalMileDeliveryRepository.save(delivery);

        // LastMileDepartedEvent 발행
        eventPublisher.publishDepartedEvent(delivery);

        log.info("배송 담당자 배정 및 출발 완료 - finalMileId: {}, driverId: {}, status: {}",
                finalMileId, driverResponse.getDriverId(), delivery.getCurrentStatus());

        return LastMileAssignDriverResponse.from(delivery, driverResponse);
    }

    /**
     * [추가] 배송 취소 (Order/Track Service에서 호출)
     *
     * @param finalMileId 배송 ID
     */
    @Transactional
    public void cancelDelivery(String finalMileId) {
        log.info("배송 취소 요청 - finalMileId: {}", finalMileId);

        FinalMileDelivery delivery = finalMileDeliveryRepository.findById(finalMileId)
                .orElseThrow(() -> new DeliveryException(
                        DeliveryErrorCode.DELIVERY_NOT_FOUND,
                        "배송 ID: " + finalMileId
                ));

        // 이미 완료된 배송은 취소 불가
        if (delivery.getCurrentStatus() == FinalMileDeliveryStatus.DELIVERED) {
            throw new DeliveryException(
                    DeliveryErrorCode.DELIVERY_ALREADY_COMPLETED,
                    "배송 ID: " + finalMileId
            );
        }

        // 이미 취소된 배송
        if (delivery.getCurrentStatus() == FinalMileDeliveryStatus.CANCELED) {
            throw new DeliveryException(
                    DeliveryErrorCode.DELIVERY_ALREADY_CANCELED,
                    "배송 ID: " + finalMileId
            );
        }

        // 드라이버에게 취소 통지 (배정된 경우에만)
        if (delivery.hasAgent()) {
            try {
                lastMileDriverClient.cancelDelivery(delivery.getAgentId());
                log.info("드라이버 취소 통지 성공 - driverId: {}", delivery.getAgentId());
            } catch (Exception e) {
                log.warn("드라이버 취소 통지 실패 - driverId: {}, error: {}",
                        delivery.getAgentId(), e.getMessage());
                // 취소 통지 실패해도 배송 취소는 진행
            }
        }

        // 배송 취소
        delivery.deliveryCancelled();
        finalMileDeliveryRepository.save(delivery);

        log.info("배송 취소 완료 - finalMileId: {}", finalMileId);
    }

    /**
     * [추가] orderId로 배송 조회
     *
     * @param orderId 주문 ID
     * @return 배송 정보
     */
    @Transactional(readOnly = true)
    public FinalMileDelivery findByOrderId(String orderId) {
        return finalMileDeliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryException(
                        DeliveryErrorCode.DELIVERY_NOT_FOUND,
                        "주문 ID: " + orderId
                ));
    }

    // ==================== [추가] Private Helper Methods ====================

    /**
     * [추가] 드라이버에게 완료 통지
     */
    private void notifyDriverCompletion(FinalMileDelivery delivery) {
        if (delivery.hasAgent()) {
            try {
                Long deliveryTimeMin = calculateDeliveryTimeMin(delivery);
                lastMileDriverClient.completeDelivery(
                        delivery.getAgentId(),
                        DriverCompleteRequest.of(deliveryTimeMin)
                );
                log.info("드라이버 완료 통지 성공 - driverId: {}, deliveryTimeMin: {}",
                        delivery.getAgentId(), deliveryTimeMin);
            } catch (Exception e) {
                log.warn("드라이버 완료 통지 실패 - driverId: {}, error: {}",
                        delivery.getAgentId(), e.getMessage());
                // 통지 실패해도 배송 완료 처리는 진행
            }
        }
    }

    /**
     * [추가] 배송 소요 시간 계산 (분)
     */
    private Long calculateDeliveryTimeMin(FinalMileDelivery delivery) {
        if (delivery.getDepartedAt() != null && delivery.getDeliveredAt() != null) {
            return Duration.between(delivery.getDepartedAt(), delivery.getDeliveredAt()).toMinutes();
        } else if (delivery.getStartedAt() != null && delivery.getDeliveredAt() != null) {
            return Duration.between(delivery.getStartedAt(), delivery.getDeliveredAt()).toMinutes();
        }
        return null;
    }
}