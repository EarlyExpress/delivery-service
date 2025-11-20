package com.early_express.delivery_service.delivery.infrastructure;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinalMileDeliveryRepository extends JpaRepository<FinalMileDelivery, String>, QuerydslPredicateExecutor<FinalMileDelivery> {

    // 1. 배송 ID로 단일 배송건 조회 (PK 기반이 아닌 필드 기반 조회)
    // PK는 JpaRepository가 기본 제공하므로, 필요에 따라 FK나 다른 필드 기반 조회를 추가합니다.
    Optional<FinalMileDelivery> findByFinalMileId(String finalMileId);

    // 2. 주문 ID로 해당 배송건 조회
    // FinalMileDelivery는 Order와 1:1 관계일 가능성이 높으므로 Optional<T>로 받습니다.
    Optional<FinalMileDelivery> findByOrderId(String orderId);

    // 3. 담당자(Agent) ID와 상태로 목록 조회
    // 담당자가 현재 배송 중인 목록이나 완료 목록을 조회할 때 사용됩니다.
    List<FinalMileDelivery> findByAgentIdAndCurrentStatus(String agentId, FinalMileDeliveryStatus currentStatus);

    // 4. 수령인 Slack ID와 상태로 목록 조회 (고객 추적용)
    List<FinalMileDelivery> findByRecipientSlackIdAndCurrentStatus(String recipientSlackId, FinalMileDeliveryStatus currentStatus);

}
