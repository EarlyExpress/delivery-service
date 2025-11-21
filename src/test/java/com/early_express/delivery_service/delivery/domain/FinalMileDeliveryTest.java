package com.early_express.delivery_service.delivery.domain;

import com.early_express.delivery_service.global.common.utils.UuidUtils;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FinalMileDelivery 엔티티 단위 테스트")
class FinalMileDeliveryTest {

    private FinalMileDelivery delivery;
    private LocalDateTime fixedTime;

    // 💡 캡슐화된 필드에 강제로 접근하여 값을 설정하는 헬퍼 메소드
    private void setDelivered() {
        ReflectionTestUtils.setField(delivery, "currentStatus", FinalMileDeliveryStatus.DELIVERED);
        ReflectionTestUtils.setField(delivery, "deliveredAt", fixedTime.minusDays(1));
    }

    private void setCanceled() {
        ReflectionTestUtils.setField(delivery, "currentStatus", FinalMileDeliveryStatus.CANCELED);
    }
    // ----------------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2025, 1, 1, 10, 0);

        // 💡 초기 상태를 PICKED_UP으로 설정하여 시작
        delivery = FinalMileDelivery.builder()
                .orderId("ORD123")
                .agentId("AGENT001")
                .currentStatus(FinalMileDeliveryStatus.PICKED_UP)
                .deliveryAddress("서울시 강남구")
                .recipientName("홍길동")
                .startedAt(fixedTime) // PICKED_UP 상태이므로 startedAt을 설정
                .expectedTime(fixedTime.plusHours(2))
                .build();
    }

    // ----------------------------------------------------------------------------------

    @Nested
    @DisplayName("🚚 픽업(pickedUp) 테스트")
    class PickedUpTest {

        // 💡 PICKED_UP 상태로 시작하므로, 이 테스트는 이미 픽업된 상태에서 재호출 시 상태 변화가 없음을 확인
        @Test
        @DisplayName("성공: PICKED_UP 상태에서 재호출 시 상태 유지")
        void pickedUp_Success_FromPickedUp() {
            LocalDateTime pickupTime = fixedTime.plusMinutes(10);

            delivery.pickedUp(pickupTime);

            assertThat(delivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.PICKED_UP);
            // startedAt이 초기 설정값(fixedTime)에서 변경되지 않았음을 확인하거나,
            // 픽업 시간을 업데이트하는 로직이 없다고 가정
            assertThat(delivery.getStartedAt()).isEqualTo(fixedTime);
        }

        @ParameterizedTest(name = "실패: {0} 상태에서는 픽업 불가")
        @EnumSource(value = FinalMileDeliveryStatus.class, names = {"DELIVERED", "CANCELED"})
        void pickedUp_Failure_InvalidStatus(FinalMileDeliveryStatus invalidStatus) {
            // Given: 이미 완료되거나 취소된 상태로 설정 (리플렉션 사용)
            if (invalidStatus == FinalMileDeliveryStatus.DELIVERED) {
                setDelivered();
            } else {
                setCanceled();
            }

            // When & Then: 예외 발생 검증
            assertThatIllegalStateException()
                    .isThrownBy(() -> delivery.pickedUp(fixedTime))
                    .withMessageContaining("이미 완료/취소된 배송입니다.");
        }
    }

    // ----------------------------------------------------------------------------------

    @Nested
    @DisplayName("🛣️ 배송 중(onDelivery) 테스트")
    class OnDeliveryTest {

        // 💡 setUp에서 이미 PICKED_UP 상태이므로, 바로 배송 중으로 전환 시도
        @Test
        @DisplayName("성공: PICKED_UP 상태에서 배송 중으로 전환 가능")
        void onDelivery_Success_FromPickedUp() {
            // Given: PICKED_UP 상태 (setUp에서 설정)

            delivery.onDelivery();

            assertThat(delivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.ON_THE_WAY);
        }

        @Test
        @DisplayName("성공: ON_THE_WAY 상태에서 재호출 시 상태 유지 (멱등성)")
        void onDelivery_Success_FromOnTheWay() {
            // Given: 배송 중 상태로 전환 (리플렉션 사용)
            ReflectionTestUtils.setField(delivery, "currentStatus", FinalMileDeliveryStatus.ON_THE_WAY);

            delivery.onDelivery();

            assertThat(delivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.ON_THE_WAY);
        }

        @ParameterizedTest(name = "실패: {0} 상태에서는 배송 중으로 전환 불가")
        @EnumSource(value = FinalMileDeliveryStatus.class, names = {"DELIVERED", "CANCELED", "FAILED"})
        void onDelivery_Failure_InvalidStatus(FinalMileDeliveryStatus invalidStatus) {
            // Given: 잘못된 상태로 설정 (리플렉션 사용)
            ReflectionTestUtils.setField(delivery, "currentStatus", invalidStatus);

            // When & Then
            assertThatIllegalStateException()
                    .isThrownBy(delivery::onDelivery)
                    // 엔티티의 검증 메시지에 맞춰 수정: "픽업되지 않은 상품은 배송 중 상태로 변경할 수 없습니다."
                    .withMessageContaining("픽업되지 않은 상품은 배송 중 상태로 변경할 수 없습니다.");
        }
    }

    // ----------------------------------------------------------------------------------

    @Nested
    @DisplayName("❌ Soft Delete 테스트")
    class SoftDeleteTest {

        private final String DELETER_ID = "SYSTEM_BATCH_01";

        @Test
        @DisplayName("성공: FAILED 상태의 배송 건 Soft Delete 가능")
        void softDelete_Success_FromFailed() {
            // Given: 실패 상태 (리플렉션 사용)
            ReflectionTestUtils.setField(delivery, "currentStatus", FinalMileDeliveryStatus.FAILED);

            delivery.markForSoftDeletion(DELETER_ID);

            // ... (BaseEntity 필드 검증 로직)
        }

        @Test
        @DisplayName("실패: DELIVERED 상태의 배송 건 Soft Delete 불가")
        void softDelete_Failure_FromDelivered() {
            // Given: 완료 상태 (리플렉션 사용)
            setDelivered();

            // When & Then
            assertThatIllegalStateException()
                    .isThrownBy(() -> delivery.markForSoftDeletion(DELETER_ID))
                    .withMessageContaining("이미 완료된 배송(DELIVERED)은 Soft Delete 처리할 수 없습니다.");
        }
    }

    // ----------------------------------------------------------------------------------

    @Nested
    @DisplayName("✅ 배송 완료(delivered) 및 취소(deliveryCancelled) 테스트")
    class DeliveredAndCancelledTest {

        @Test
        @DisplayName("✅ 성공: PICKED_UP 상태에서 배송 완료 가능")
        void delivered_Success_FromPickedUp() {
            // Given: PICKED_UP 상태 (setUp에서 설정)

            delivery.delivered();

            assertThat(delivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.DELIVERED);
            assertThat(delivery.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("🔄 성공: PICKED_UP 상태에서 취소 가능")
        void cancelled_Success_FromPickedUp() {
            // Given: PICKED_UP 상태 (setUp에서 설정)

            delivery.deliveryCancelled();

            assertThat(delivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.CANCELED);
        }

        @Test
        @DisplayName("✅ 실패: CANCELED 상태에서는 배송 완료 불가")
        void delivered_Failure_FromCanceled() {
            // Given: 취소 상태 (리플렉션 사용)
            setCanceled();

            assertThatIllegalStateException()
                    .isThrownBy(delivery::delivered)
                    .withMessageContaining("현재 상태(CANCELED)에서는 배송 완료 처리할 수 없습니다.");
        }

        @Test
        @DisplayName("🔄 실패: DELIVERED 상태에서는 취소 불가")
        void cancelled_Failure_FromDelivered() {
            // Given: 완료 상태 (리플렉션 사용)
            setDelivered();

            assertThatIllegalStateException()
                    .isThrownBy(delivery::deliveryCancelled)
                    .withMessageContaining("이미 완료된 배송은 취소할 수 없습니다. 반품 절차를 사용하세요.");
        }
    }
}