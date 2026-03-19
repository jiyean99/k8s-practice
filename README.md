# Kubernetes & DevOps 실습 정리

Spring Boot 기반 서비스를 Kubernetes(EKS)에 배포하는 과정을 단계별로 학습한 실습 레포지토리입니다.

---

## 목차

1. [1.k8s_basic - 쿠버네티스 기본 개념](#1k8s_basic)
2. [2.ordersystem - 단일 서비스 K8s 배포](#2ordersystem)
3. [3.msa - 마이크로서비스 아키텍쳐 K8s 배포](#3msa)
4. [CI/CD 파이프라인](#cicd)
5. [공통 패턴 및 핵심 개념](#핵심-개념)

---

## 1.k8s_basic

> 쿠버네티스 핵심 오브젝트를 직접 YAML로 작성하며 익히는 기초 실습

### 학습 내용

#### Pod 기본 (`1.pod_basic/`)

| 파일 | 설명 |
|---|---|
| `nginx_pod.yml` | 단일 Pod 생성, Namespace(jiyean-ns), Label 설정 |
| `nginx_service.yml` | ClusterIP Service로 Pod 내부 통신 |
| `nginx_pod_busybox.yml` | 멀티 컨테이너 Pod (nginx + busybox 헬스체크) |

**핵심 포인트**
- 같은 Pod 내 컨테이너는 `localhost`로 통신 (네트워크 네임스페이스 공유)
- Service의 `selector`가 Pod의 `labels`와 일치해야 연결됨
- `busybox`로 wget 헬스체크하여 컨테이너 상태 확인

```
┌─────────────────────────────────┐
│          Pod (nginx-pod)        │
│  ┌──────────┐  ┌─────────────┐ │
│  │  nginx   │  │   busybox   │ │
│  │  :80     │←─│  wget check │ │
│  └──────────┘  └─────────────┘ │
└─────────────────────────────────┘
        ↑
  ClusterIP Service
```

#### 다중 Pod & 트래픽 라우팅 (`2.multi_pod/`)

| 파일 | 설명 |
|---|---|
| `nginx_deployment.yml` | Deployment로 2 Replica 관리, 롤링 업데이트 |
| `nginx_replicaset_and_service.yml` | ReplicaSet + Service 조합 |
| `nginx_ingress.yml` | 외부 도메인(server.eazy99.shop) 트래픽 진입점 |

**Deployment vs ReplicaSet**
- `ReplicaSet`: Pod 수를 유지하는 역할
- `Deployment`: ReplicaSet을 래핑하여 롤링 업데이트, 롤백 기능 추가

**Ingress 경로 기반 라우팅 예시**
```yaml
# 경로별로 다른 서비스로 라우팅 가능
/api      → backend-service:8080
/static   → frontend-service:80
/admin    → admin-service:8080
```

---

## 2.ordersystem

> Spring Boot 단일 서비스를 MariaDB, Redis와 함께 K8s 클러스터에 배포

### 아키텍쳐

```
인터넷
  │
  ▼
[Ingress - NGINX]
  server.eazy99.shop (HTTPS/TLS)
  │
  ▼
[ordersystem Service - ClusterIP :80]
  │
  ▼
[ordersystem Deployment]
  Spring Boot :8080
  ├── MariaDB (DB_HOST Secret으로 주입)
  ├── Redis :6379 (세션/캐시)
  └── AWS S3 (상품 이미지)
```

### 기술 스택

| 역할 | 기술 |
|---|---|
| 백엔드 | Spring Boot, Spring Security, Spring Data JPA |
| DB | MariaDB |
| 캐시 | Redis |
| 스토리지 | AWS S3 |
| 인프라 | Kubernetes(EKS), AWS ECR |

### K8s 오브젝트 구성

#### Deployment (`ordersystem_depl_svc.yml`)
```yaml
replicas: 1
image: AWS ECR
resources:
  limits:   { cpu: 1, memory: 500Mi }
  requests: { cpu: 0.5, memory: 250Mi }
readinessProbe:
  path: /health
  initialDelaySeconds: 10
  periodSeconds: 10
env:
  DB_HOST: Secret에서 주입
  DB_PW:   Secret에서 주입
```

#### Redis (`ordersystem_redis_depl.yml`)
- ClusterIP로 내부 전용 노출
- 애플리케이션에서 `my-redis:6379`로 접근 (K8s DNS 활용)

#### HPA (`hpa.yml`)
```yaml
minReplicas: 1
maxReplicas: 3
scaleTarget: CPU 사용률 50% 초과 시 Scale Out
```

#### HTTPS 자동화 (`https.yml`)
- `cert-manager` + Let's Encrypt ACME로 TLS 인증서 자동 발급
- 90일 만료, 15일 전 자동 갱신
- HTTP-01 챌린지 방식 (NGINX 인그레스 사용)

### 학습 포인트

- **Secret**: DB 비밀번호 등 민감 정보를 환경변수로 안전하게 주입
- **ReadinessProbe**: 서비스가 준비됐을 때만 트래픽 수신 (무중단 배포 핵심)
- **HPA**: 트래픽 증가 시 Pod 자동 증설
- **cert-manager**: HTTPS 인증서 갱신을 자동화하여 운영 부담 제거

---

## 3.msa

> 4개의 독립 Spring Boot 서비스를 API Gateway, Kafka, Redis와 함께 K8s에 배포하는 마이크로서비스 아키텍쳐

### 전체 아키텍쳐

```
인터넷
  │
  ▼
[Ingress - NGINX]
  server.eazy99.shop (HTTPS/TLS)
  │
  ▼
[apigateway-service - ClusterIP :80]
  Spring Cloud Gateway :8080
  │
  ├─────────────────────────────────────────┐
  │                                         │
  ▼                                         ▼
[member-service :8080]           [ordering-service :8080]
  사용자 관리                        주문 처리
  └── Redis (세션/캐시)               ├── http://product-service (K8s DNS)
                                       └── kafka-service:9092 (비동기 메시징)

[product-service :8080]
  상품 관리
  ├── AWS S3 (이미지)
  └── kafka-service:9092 (재고 업데이트 구독)

[공유 인프라]
  ├── kafka-service:9092   (Apache Kafka 3.7.0 KRaft 모드)
  └── redis-service:6379   (Redis)
```

### 마이크로서비스 구성

#### API Gateway (`apigateway/`)
- 모든 외부 요청의 단일 진입점
- Spring Cloud Gateway로 각 서비스로 라우팅
- `/health` 엔드포인트로 Readiness Probe

#### Member Service (`member/`)
- 사용자 인증/관리
- Redis를 세션 저장소로 사용
- Eureka 비활성화 → K8s Service DNS로 서비스 디스커버리 대체

#### Ordering Service (`ordering/`)
- 주문 생성/조회
- **동기 통신**: `http://product-service` (K8s DNS)로 상품 정보 조회
- **비동기 통신**: Kafka를 통해 재고 차감 이벤트 발행

#### Product Service (`product/`)
- 상품 등록/조회, 이미지 관리
- Kafka Consumer: `product-group`으로 재고 업데이트 메시지 수신
- AWS S3에 상품 이미지 저장

### 인프라 구성

#### Kafka (`kafka_depl_svc.yml`)
```yaml
image: apache/kafka:3.7.0
mode: KRaft (ZooKeeper 불필요, 단일 노드)
ports:
  9092: 클라이언트 통신
  9093: 컨트롤러 통신
auto.create.topics: true
```

**KRaft 모드**: ZooKeeper 없이 Kafka 자체 합의 알고리즘으로 동작 (3.7+ 권장)

#### 서비스 간 통신 방식

| 방식 | 사용 사례 | 구현 |
|---|---|---|
| 동기 (HTTP) | 주문 시 상품 정보 즉시 조회 | `http://product-service` (K8s DNS) |
| 비동기 (Kafka) | 재고 차감, 이벤트 전파 | `kafka-service:9092` |

#### K8s Service Discovery (Eureka 대체)
```
# 유레카 서버 없이 K8s 내부 DNS로 서비스 발견
ordering → product : http://product-service
member   → redis   : redis-service:6379
ordering → kafka   : kafka-service:9092
```

> Eureka는 개발환경(local profile)에서만 사용, 프로덕션에서는 비활성화

### Dockerfile (멀티 스테이지 빌드)

모든 서비스가 동일한 패턴 사용:

```dockerfile
# Stage 1: 빌드
FROM eclipse-temurin:17-jdk-alpine AS builder
COPY . .
RUN ./gradlew bootJar

# Stage 2: 실행 (최소 이미지)
FROM eclipse-temurin:17-jdk-alpine
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
```

**효과**: 빌드 도구, 소스코드 제외 → 최종 이미지 크기 약 50% 감소

### DB 커넥션 풀 최소화
```yaml
# MSA 환경에서 각 서비스의 커넥션 풀 최소화
spring.datasource.hikari.maximum-pool-size: 1
```
서비스 수가 많을수록 DB 커넥션 총량 관리가 중요 → 각 서비스가 최소한의 커넥션만 사용

---

## CI/CD

### GitHub Actions → EKS 자동 배포 (`.github/workflows/deploy-msa-k8s.yml`)

```
코드 Push (main 브랜치)
  │
  ▼
[GitHub Actions]
  1. kubectl 설치 (v1.25.9)
  2. AWS 자격증명 설정 (Secrets에서 주입)
  3. kubeconfig 업데이트 (EKS: 5team-cluster)
  4. ECR 로그인
  │
  ▼ (4개 서비스 병렬)
  각 서비스별:
    ① docker build
    ② ECR push (latest 태그)
    ③ kubectl apply (매니페스트 적용)
    ④ kubectl rollout restart (새 이미지로 재시작)
```

**핵심**: `rollout restart`로 이미지는 `latest`이지만 새로운 Pod을 강제 재시작하여 최신 이미지 반영

---

## 핵심 개념

### K8s 오브젝트 역할 정리

| 오브젝트 | 역할 |
|---|---|
| `Pod` | 컨테이너 실행 단위 |
| `ReplicaSet` | Pod 수 유지 |
| `Deployment` | ReplicaSet 관리 + 롤링 업데이트 |
| `Service (ClusterIP)` | 클러스터 내부 통신 / DNS 이름 부여 |
| `Ingress` | 외부 → 클러스터 트래픽 진입, 도메인/경로 기반 라우팅 |
| `HPA` | CPU/메모리 기준 Pod 자동 증감 |
| `Secret` | 민감 정보(비밀번호, 키) 안전하게 주입 |

### 네트워크 흐름

```
외부 사용자
  │ HTTPS
  ▼
Ingress (NGINX) ← cert-manager (TLS 자동화)
  │ HTTP (클러스터 내부)
  ▼
Service (ClusterIP) ← K8s DNS: {서비스명}.{네임스페이스}.svc.cluster.local
  │
  ▼
Pod (컨테이너)
```

### 환경별 프로파일 전략

```
application.yml         ← 공통 설정 (profile: prod 지정)
application-local.yml   ← 로컬 개발 (Eureka 활성, 로컬 DB)
application-prod.yml    ← 운영 배포 (K8s DNS, Eureka 비활성)
```

### Namespace 분리

- 모든 리소스를 `jiyean-ns` 네임스페이스로 격리
- 팀/프로젝트 단위 리소스 관리, RBAC 권한 제어에 활용

---

## 기술 스택 요약

| 영역 | 기술 |
|---|---|
| 컨테이너 오케스트레이션 | Kubernetes (AWS EKS) |
| API 게이트웨이 | Spring Cloud Gateway |
| 백엔드 프레임워크 | Spring Boot 3.x (Java 17) |
| 데이터베이스 | MariaDB |
| 캐시 | Redis |
| 메시지 큐 | Apache Kafka 3.7.0 (KRaft) |
| 인증서 관리 | cert-manager + Let's Encrypt |
| 인그레스 | NGINX Ingress Controller |
| 컨테이너 레지스트리 | AWS ECR |
| CI/CD | GitHub Actions |
| 빌드 도구 | Gradle |
