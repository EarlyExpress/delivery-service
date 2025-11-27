package com.early_express.delivery_service.delivery.infrastructure;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    /**
     * 특정 허브의 당일 배송 목록 조회 (ASSIGNED 상태, 담당자 배정됨)
     * - expectedTime이 오늘 날짜인 배송건만 조회
     */
    @Query("SELECT f FROM FinalMileDelivery f " +
            "WHERE f.hubId = :hubId " +
            "AND f.currentStatus = 'ASSIGNED' " +
            "AND f.agentId IS NOT NULL " +
            "AND f.expectedTime >= :startOfDay " +
            "AND f.expectedTime < :endOfDay " +
            "AND f.isDeleted = false " +
            "ORDER BY f.agentId, f.expectedTime")
    List<FinalMileDelivery> findTodayAssignedDeliveriesByHub(
            @Param("hubId") String hubId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 당일 배송 목록 전체 조회 (모든 허브, ASSIGNED 상태)
     */
    @Query("SELECT f FROM FinalMileDelivery f " +
            "WHERE f.currentStatus = 'ASSIGNED' " +
            "AND f.agentId IS NOT NULL " +
            "AND f.expectedTime >= :startOfDay " +
            "AND f.expectedTime < :endOfDay " +
            "AND f.isDeleted = false " +
            "ORDER BY f.agentId, f.expectedTime")
    List<FinalMileDelivery> findTodayAssignedDeliveries(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 특정 담당자의 당일 배송 목록 조회
     */
    @Query("SELECT f FROM FinalMileDelivery f " +
            "WHERE f.agentId = :agentId " +
            "AND f.currentStatus IN ('ASSIGNED', 'PICKED_UP', 'ON_THE_WAY') " +
            "AND f.expectedTime >= :startOfDay " +
            "AND f.expectedTime < :endOfDay " +
            "AND f.isDeleted = false " +
            "ORDER BY f.expectedTime")
    List<FinalMileDelivery> findTodayDeliveriesByAgent(
            @Param("agentId") String agentId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}
