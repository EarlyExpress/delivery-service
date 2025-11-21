package com.early_express.delivery_service.delivery.application.service;

import com.early_express.delivery_service.delivery.domain.FinalMileDelivery;
import com.early_express.delivery_service.delivery.domain.FinalMileDeliveryStatus;
import com.early_express.delivery_service.delivery.infrastructure.FinalMileDeliveryRepository;
import com.early_express.delivery_service.delivery.presentation.rest.dto.DeliveryStatusUpdateRequest;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryDetailResponse;
import com.early_express.delivery_service.delivery.presentation.rest.dto.FinalMileDeliveryRequest;
import com.early_express.delivery_service.global.presentation.exception.DeliveryNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalMileDeliveryServiceTest {

    // Mocking할 의존성 객체
    @Mock
    private FinalMileDeliveryRepository finalMileDeliveryRepository;

    // 테스트 대상 객체 (Mock 객체가 주입됨)
    @InjectMocks
    private FinalMileDeliveryService finalMileDeliveryService;

    // 테스트에서 사용할 고정 시간 및 ID
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 1, 1, 10, 0);
    private final String AGENT_ID = "AGENT001";
    private final String FINAL_MILE_ID = "FMID123";

    // Mockito 정적 Mock (LocalDateTime.now()를 제어하기 위함)
    private MockedStatic<LocalDateTime> localDateTimeMockedStatic;

    // 테스트 데이터
    private FinalMileDeliveryRequest request;

    @BeforeEach
    void setUp() {
        request = new FinalMileDeliveryRequest(
                "ORD999",
                "서울시 강남구",
                "김철수",
                "slack-kcs",
                FIXED_TIME.plusHours(2)
        );

        // FinalMileDeliveryService 내에서 LocalDateTime.now()를 사용하는 메소드 테스트를 위해 시간을 고정
        localDateTimeMockedStatic = mockStatic(LocalDateTime.class);
        localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(FIXED_TIME);
    }

    @AfterEach
    void tearDown() {
        // 정적 Mock 해제
        localDateTimeMockedStatic.close();
    }


    // -----------------------------------------------------------
    // 1. 배송 등록 (registerDelivery) 테스트
    // -----------------------------------------------------------
    @Nested
    @DisplayName("1. 배송 등록 (registerDelivery)")
    class RegisterDeliveryTest {

        @Test
        @DisplayName("성공: 요청 데이터로 FinalMileDelivery가 생성 및 저장되며 ID가 반환되어야 한다.")
        void registerDelivery_Success() {
            // Given
            // Service 구현체는 save(delivery)의 반환값(savedDelivery)에서 ID를 추출함.
            // 따라서 save 호출 시 Mock 엔티티에 ID를 설정하고 그 엔티티를 반환하도록 Mocking 해야 함.
            when(finalMileDeliveryRepository.save(any(FinalMileDelivery.class)))
                    .thenAnswer(invocation -> {
                        FinalMileDelivery savedDelivery = invocation.getArgument(0);
                        // Reflection을 사용하여 ID 필드를 임의로 설정
                        ReflectionTestUtils.setField(savedDelivery, "finalMileId", FINAL_MILE_ID);
                        return savedDelivery; // ID가 설정된 엔티티 반환
                    });

            // When
            String resultId = finalMileDeliveryService.registerDelivery(AGENT_ID, request);

            // Then
            // 1. 레포지토리의 save 메소드 호출 검증
            verify(finalMileDeliveryRepository, times(1)).save(any(FinalMileDelivery.class));

            // 2. 반환 ID 검증
            assertThat(resultId).isEqualTo(FINAL_MILE_ID);

            // 3. 저장된 엔티티의 필드 검증 (save 호출 시 인수로 전달된 객체 확인)
            ArgumentCaptor<FinalMileDelivery> deliveryCaptor = ArgumentCaptor.forClass(FinalMileDelivery.class);
            verify(finalMileDeliveryRepository).save(deliveryCaptor.capture());
            FinalMileDelivery capturedDelivery = deliveryCaptor.getValue();

            // .pickedUp(now) 호출로 인해 상태와 시작 시간이 설정되었는지 확인
            assertThat(capturedDelivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.PICKED_UP);
            assertThat(capturedDelivery.getStartedAt()).isEqualTo(FIXED_TIME); // Mocking된 LocalDateTime.now()

            // 기타 필드 검증
            assertThat(capturedDelivery.getAgentId()).isEqualTo(AGENT_ID);
            assertThat(capturedDelivery.getOrderId()).isEqualTo(request.orderId());
        }
    }

    // -----------------------------------------------------------
    // 2. 배송 상세 조회 (getDeliveryDetail) 테스트
    // -----------------------------------------------------------
    @Nested
    @DisplayName("2. 배송 상세 조회 (getDeliveryDetail)")
    class GetDeliveryDetailTest {

        private FinalMileDelivery mockDelivery;

        @BeforeEach
        void setup() {
            // 테스트용 Mock 엔티티 생성
            mockDelivery = FinalMileDelivery.builder()
                    .orderId("ORD100")
                    .agentId("AGENT002")
                    .currentStatus(FinalMileDeliveryStatus.ON_THE_WAY)
                    .startedAt(FIXED_TIME.minusHours(1))
                    .build();

            // ✅ 수정된 핵심 부분: Mock 엔티티에 finalMileId를 설정하여 DTO 변환 시 ID가 null이 되는 것을 방지
            // 이 수정으로 이전에 발생했던 "expected: "FMID123" but was: null" 에러를 해결
            ReflectionTestUtils.setField(mockDelivery, "finalMileId", FINAL_MILE_ID);
        }

        @Test
        @DisplayName("성공: ID에 해당하는 배송 상세 정보를 FinalMileDeliveryDetailResponse로 반환해야 한다.")
        void getDeliveryDetail_Success() {
            // Given
            when(finalMileDeliveryRepository.findById(FINAL_MILE_ID)).thenReturn(Optional.of(mockDelivery));

            // When
            FinalMileDeliveryDetailResponse response = finalMileDeliveryService.getDeliveryDetail(FINAL_MILE_ID);

            // Then
            assertThat(response.finalMileId()).isEqualTo(FINAL_MILE_ID);
            assertThat(response.currentStatus()).isEqualTo(FinalMileDeliveryStatus.ON_THE_WAY);
            assertThat(response.agentId()).isEqualTo("AGENT002");
            assertThat(response.orderId()).isEqualTo("ORD100");
            verify(finalMileDeliveryRepository, times(1)).findById(FINAL_MILE_ID);
        }

        @Test
        @DisplayName("실패: ID에 해당하는 배송 정보가 없을 경우 DeliveryNotFoundException 예외를 발생시켜야 한다.")
        void getDeliveryDetail_NotFound() {
            // Given
            when(finalMileDeliveryRepository.findById(FINAL_MILE_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> finalMileDeliveryService.getDeliveryDetail(FINAL_MILE_ID))
                    .isInstanceOf(DeliveryNotFoundException.class)
                    .hasMessageContaining(FINAL_MILE_ID);
        }
    }

    // -----------------------------------------------------------
    // 3. 배송 상태 업데이트 (updateDeliveryStatus) 테스트
    // -----------------------------------------------------------
    @Nested
    @DisplayName("3. 배송 상태 업데이트 (updateDeliveryStatus)")
    class UpdateDeliveryStatusTest {

        private FinalMileDelivery mockDelivery;

        @BeforeEach
        void setup() {
            // 초기 상태는 PICKED_UP으로 설정
            mockDelivery = FinalMileDelivery.builder()
                    .currentStatus(FinalMileDeliveryStatus.PICKED_UP)
                    .build();

            // 모든 상태 업데이트 테스트에서 엔티티 조회를 Mocking
            when(finalMileDeliveryRepository.findById(FINAL_MILE_ID)).thenReturn(Optional.of(mockDelivery));
        }

        @Test
        @DisplayName("성공: ON_THE_WAY로 상태가 변경되어야 한다.")
        void updateStatus_To_OnTheWay_Success() {
            // Given
            DeliveryStatusUpdateRequest updateReq = new DeliveryStatusUpdateRequest(FinalMileDeliveryStatus.ON_THE_WAY);

            // When
            finalMileDeliveryService.updateDeliveryStatus(FINAL_MILE_ID, updateReq);

            // Then
            // delivery.onDelivery() 호출 후 상태 확인
            assertThat(mockDelivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.ON_THE_WAY);
            verify(finalMileDeliveryRepository, times(1)).findById(FINAL_MILE_ID);
            verify(finalMileDeliveryRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공: DELIVERED로 상태가 변경되고 deliveredAt이 설정되어야 한다.")
        void updateStatus_To_Delivered_Success() {
            // Given
            DeliveryStatusUpdateRequest updateReq = new DeliveryStatusUpdateRequest(FinalMileDeliveryStatus.DELIVERED);

            // When
            finalMileDeliveryService.updateDeliveryStatus(FINAL_MILE_ID, updateReq);

            // Then
            // delivery.delivered() 호출 후 상태 및 시간 확인
            assertThat(mockDelivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.DELIVERED);
            assertThat(mockDelivery.getDeliveredAt()).isEqualTo(FIXED_TIME); // Mocking된 LocalDateTime.now() 사용
        }

        @Test
        @DisplayName("성공: CANCELED로 상태가 변경되어야 한다.")
        void updateStatus_To_Canceled_Success() {
            // Given
            DeliveryStatusUpdateRequest updateReq = new DeliveryStatusUpdateRequest(FinalMileDeliveryStatus.CANCELED);

            // When
            finalMileDeliveryService.updateDeliveryStatus(FINAL_MILE_ID, updateReq);

            // Then
            assertThat(mockDelivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.CANCELED);
        }

        @Test
        @DisplayName("성공: FAILED로 상태가 변경되어야 한다.")
        void updateStatus_To_Failed_Success() {
            // Given
            DeliveryStatusUpdateRequest updateReq = new DeliveryStatusUpdateRequest(FinalMileDeliveryStatus.FAILED);

            // When
            finalMileDeliveryService.updateDeliveryStatus(FINAL_MILE_ID, updateReq);

            // Then
            assertThat(mockDelivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.FAILED);
        }

        @Test
        @DisplayName("성공: 이미 PICKED_UP 상태일 때, PICKED_UP 요청 시 상태가 유지되어야 한다.")
        void updateStatus_To_PickedUp_WhenAlreadyPickedUp_Success() {
            // Given
            DeliveryStatusUpdateRequest updateReq = new DeliveryStatusUpdateRequest(FinalMileDeliveryStatus.PICKED_UP);

            // When
            finalMileDeliveryService.updateDeliveryStatus(FINAL_MILE_ID, updateReq);

            // Then
            // 서비스 로직의 if (delivery.getCurrentStatus() != FinalMileDeliveryStatus.PICKED_UP) 조건에 의해 상태 변경이 무시됨.
            assertThat(mockDelivery.getCurrentStatus()).isEqualTo(FinalMileDeliveryStatus.PICKED_UP);
        }

        @Test
        @DisplayName("실패: 잘못된 ID로 조회 시 DeliveryNotFoundException 예외가 발생해야 한다.")
        void updateStatus_NotFound() {
            // Given
            when(finalMileDeliveryRepository.findById(FINAL_MILE_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> finalMileDeliveryService.updateDeliveryStatus(FINAL_MILE_ID, new DeliveryStatusUpdateRequest(FinalMileDeliveryStatus.DELIVERED)))
                    .isInstanceOf(DeliveryNotFoundException.class);
        }

        @Test
        @DisplayName("실패: 엔티티의 도메인 규칙 위반 시 IllegalStateException이 발생해야 한다.")
        void updateStatus_DomainRuleViolation() {
            // Given: 취소된 상태에서 배송 완료 요청 시도 -> FinalMileDelivery 내부에서 검증
            ReflectionTestUtils.setField(mockDelivery, "currentStatus", FinalMileDeliveryStatus.CANCELED);
            DeliveryStatusUpdateRequest updateReq = new DeliveryStatusUpdateRequest(FinalMileDeliveryStatus.DELIVERED);

            // When & Then
            assertThatThrownBy(() -> finalMileDeliveryService.updateDeliveryStatus(FINAL_MILE_ID, updateReq))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("CANCELED"); // 예외 메시지에 이전 상태가 포함되어 있는지 확인
        }
    }

    // -----------------------------------------------------------
    // 4. Soft Delete (softDeleteDelivery) 테스트
    // -----------------------------------------------------------
    @Nested
    @DisplayName("4. Soft Delete (softDeleteDelivery)")
    class SoftDeleteDeliveryTest {

        private FinalMileDelivery mockDelivery;
        private final String DELETER_ID = "SYS_ADMIN";

        @BeforeEach
        void setup() {
            // Soft Delete가 가능한 FAILED 상태로 초기화
            mockDelivery = FinalMileDelivery.builder()
                    .currentStatus(FinalMileDeliveryStatus.FAILED)
                    .build();

            when(finalMileDeliveryRepository.findById(FINAL_MILE_ID)).thenReturn(Optional.of(mockDelivery));
        }

        @Test
        @DisplayName("성공: Soft Delete 가능 상태에서 정상 처리되어야 한다.")
        void softDeleteDelivery_Success() {
            // When
            finalMileDeliveryService.softDeleteDelivery(FINAL_MILE_ID, DELETER_ID);

            // Then
            // delivery.markForSoftDeletion(deletedBy) 호출 후 상태 확인
            assertThat(ReflectionTestUtils.getField(mockDelivery, "isDeleted")).isEqualTo(true);
            assertThat(ReflectionTestUtils.getField(mockDelivery, "deletedBy")).isEqualTo(DELETER_ID);

            verify(finalMileDeliveryRepository, times(1)).findById(FINAL_MILE_ID);
        }

        @Test
        @DisplayName("실패: DELIVERED 상태에서는 Soft Delete가 불가능하며 IllegalStateException이 발생해야 한다.")
        void softDeleteDelivery_RuleViolation() {
            // Given: DELIVERED 상태로 변경
            ReflectionTestUtils.setField(mockDelivery, "currentStatus", FinalMileDeliveryStatus.DELIVERED);

            // When & Then
            assertThatThrownBy(() -> finalMileDeliveryService.softDeleteDelivery(FINAL_MILE_ID, DELETER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 완료된 배송(DELIVERED)은 Soft Delete 처리할 수 없습니다.");
        }

        @Test
        @DisplayName("실패: 잘못된 ID로 조회 시 DeliveryNotFoundException 예외가 발생해야 한다.")
        void softDeleteDelivery_NotFound() {
            // Given
            when(finalMileDeliveryRepository.findById(FINAL_MILE_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> finalMileDeliveryService.softDeleteDelivery(FINAL_MILE_ID, DELETER_ID))
                    .isInstanceOf(DeliveryNotFoundException.class);
        }
    }
}