## 🌟 EarlyExpress: 허브 기반 통합 물류 플랫폼 README

---

## 1. 프로젝트 개요 (Project Overview)

### 1.1. 서비스 소개
**EarlyExpress**는 전국 **17개 지역 물류 허브**를 중심으로 운영되는 기업용 통합 물류 플랫폼입니다. 전통적인 수작업 기반 물류 운영의 비효율성을 해소하고, **클라우드 기반 MSA 아키텍처**로 재설계하여 허브 간 물류 이동, 배송, 업체 관리의 효율성과 안정성을 극대화합니다.

### 1.2. 목표 및 비전
* **목표**: 복잡하게 얽힌 물류 도메인을 독립적인 마이크로 서비스로 분리하고 자동화하여, 대규모 물류 시스템의 기술적 난제를 **교육/학습 가능한 수준**으로 단순화하여 해결하는 경험을 제공합니다.
* **비전**: 확장성과 장애 대응력을 갖춘 차세대 물류 플랫폼의 핵심 구조를 구현하고 검증합니다.

---

## 2. 문제 정의 및 해결 전략 (Problem & Solution)

### 2.1. 기존 물류 운영의 주요 문제점
| 문제점 | 영향 |
| :--- | :--- |
| **복잡한 경로 관리** | 허브 간 이동 및 배송 라우팅 비효율, 수작업 관리의 한계 |
| **정보 분산** | 생산업체/수령업체 정보 파악 어려움, 전체 물류 흐름 추적 불가 |
| **확장성/안정성 부족** | 단일 시스템(Monolithic) 운영으로 인한 장애 대응 취약 및 규모 확장 제한 |

### 2.2. EarlyExpress의 MSA 기반 해결 전략
* **도메인 분리 및 추상화**: 허브, 업체, 배송, 주문 등 복잡한 도메인을 **독립적인 마이크로 서비스**로 분리하여 관리.
* **자동화된 통제**: Keycloak 기반 **RBAC**를 적용하여 일관된 **권한 기반 접근 통제**를 구현.
* **비동기 처리**: **Apache Kafka**를 활용하여 이벤트(이동/배송)를 비동기 처리, 서비스 간 **결합도를 낮추고** 처리 속도를 향상.

---

## 3. 핵심 물류 흐름 (Core Logistics Flow) 및 순서도

EarlyExpress 플랫폼 내에서 ITEM이 허브에 입고되어 최종 고객에게 배송되기까지의 주요 흐름을 나타냅니다.

### 3.1. 물류 처리 순서도 (Flowchart)

ITEM은 Hub Service에서 등록 및 관리된 후, Delivery Service로 배정되어 Last Mile Delivery 단계를 거칩니다.


1.  **Order Generation (주문 생성)**: **Vendor Service**에서 주문 및 업체 정보 등록.
2.  **Hub Inbound (허브 입고)**: ITEM이 지역 허브에 도착하면 **Hub Service**에서 입고 처리.
3.  **Assignment & Routing (배정 및 경로)**: 출고 예약된 ITEM이 **Delivery Service**로 전달되어 배송원에게 할당되고 최적 경로가 계산됨.
4.  **In-Transit Event (이동 이벤트)**: 허브 간 이동 및 배송 상태 변경 시 **Kafka**를 통해 비동기 이벤트 발생.
5.  **Last Mile Delivery (최종 배송)**: 배송원이 **`start`** 및 **`complete`** API를 통해 배송을 진행하고 최종 완료 처리.

---

## 4. 기술 스택 및 핵심 컴포넌트 (Technology Stack)

| 영역 | 기술 | 주요 특징 및 역할 |
| :--- | :--- | :--- |
| **Backend** | **Spring Boot 3.x** | 코어 애플리케이션 개발 프레임워크 |
| **MSA Gateway** | **Spring Cloud Gateway** | 모든 API 트래픽의 단일 진입점, 인증/로그/라우팅 제어 |
| **Service Discovery** | **Spring Cloud Eureka** | 서비스 위치 자동 등록/조회 및 동적 라우팅 지원 |
| **Messaging** | **Apache Kafka** | 허브 이동/배송 이벤트의 비동기 처리 및 낮은 결합도 유지 |
| **Authentication** | **Keycloak + JWT (RBAC)** | 역할 기반 권한 체계 구현 및 토큰 기반 인증 제공 |
| **Observability** | **Prometheus + Grafana + Loki** | 실시간 운영 지표 수집 및 시각화, 로그 중앙화 |
| **Containerization** | **Docker & Docker Compose** | 개발/배포 환경 표준화 및 다중 컴포넌트 환경 구축 |

---

## 5. 📝 주요 API 명세 (Key API Endpoints)

### 5.1. Hub Service (허브 관리 서비스)
| HTTP Method | Endpoint (Gateway 기준) | 설명 |
| :--- | :--- | :--- |
| `GET` | **`/v1/product/web/hub-manager/hub-products`** | 허브 관리자가 현재 허브 내 ITEM 목록을 조회합니다. |
| `POST` | `/v1/product/hub/{hubId}/inbound` | 외부로부터 도착한 ITEM을 시스템에 입고 처리합니다. |

### 5.2. 🚚 Last Mile Delivery Service (최종 업체배송 서비스)
이 서비스는 최종 배송 단계의 운영을 담당합니다.

| HTTP Method | Endpoint (Gateway 기준) | 역할 |
| :--- | :--- | :--- |
| `GET` | **`/v1/last-mile/web/drivers/my-deliveries`** | 배송원 앱에서 자신에게 할당된 배송 목록을 조회합니다. |
| `POST` | **`/v1/last-mile/web/drivers/start/{id}`** | 특정 배송 건에 대해 **배송 시작**을 기록합니다. (상태: `IN_TRANSIT` 변경) |
| `POST` | **`/v1/last-mile/web/drivers/complete/{id}`** | 배송 건을 최종 배송지에 전달하고 **배송 완료 처리**를 합니다. |
| `POST` | **`/v1/last-mile/web/drivers/signature/{id}`** | 배송 완료 시 고객 서명 또는 수령 사진을 증빙 자료로 업로드합니다. |
| `GET` | **`/v1/last-mile/web/receiver/track/{id}`** | 수령업체/고객이 배송 번호를 통해 **실시간 배송 상태와 위치**를 추적합니다. |

---

## 6. 프로젝트 정보 및 일정 (Project Information & Schedule)

| 구분 | 기간 | 비고 |
| :--- | :--- | :--- |
| **자료 조사 및 학습 기간** | 11월 3일 ~ 11월 14일 (11일) | 기술 스택 학습 및 도메인 분석 |
| **프로젝트 개발 기간** | 11월 17일 ~ 11월 27일 (10일) | 핵심 서비스 구현 및 통합 테스트 |

---

## 7. 개발 및 실행 환경 (Development and Execution Environment)

* **필수 요구 사항**: Java Development Kit (JDK) 17+, Docker 및 Docker Compose
* **실행 절차**:
    1.  Git Repository Clone
    2.  각 마이크로 서비스 빌드 (`./mvnw clean package`)
    3.  `docker-compose up --build` 명령어로 모든 컴포넌트 실행

---

## 8. 담당자 연락처 (Contacts)

| 역할 | 담당자 | 연락처 (이메일/채널) |
| :--- | :--- | :--- |
| **프로젝트 리드** | [이름] | [연락처] |
| **Backend 개발** | [이름] | [연락처] |

---

이 README는 EarlyExpress 프로젝트의 구조, 흐름, 그리고 핵심 기능을 명확하게 전달하기 위해 작성되었습니다.

이 외에 추가적인 시스템 다이어그램이나 기술적인 심화 내용 (예: Keycloak RBAC 상세 역할 구조)을 보강해 드릴까요?
