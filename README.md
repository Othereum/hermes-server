# Hermes

> 엔터프라이즈급 인사 관리 마이크로서비스 SaaS 시스템

Hermes는 멀티테넌트 아키텍처 기반의 Spring Boot 마이크로서비스 SaaS 시스템으로, 기업 인력 관리를 위한 포괄적인 기능을 제공합니다.

## 주요 기능

- 🔐 **사용자 관리**: JWT 기반 인증/인가, 역할 기반 접근 제어
- 🏢 **조직 관리**: 계층적 조직 구조 및 직원 배치 관리
- ⏰ **근태 관리**: 출퇴근 기록 및 근무 시간 추적
- 📝 **결재 시스템**: 문서 결재 워크플로우 및 템플릿 관리
- 🏪 **기업정보 관리**: 회사 정보 및 설정 관리
- 📰 **뉴스 크롤링**: 뉴스 기사 수집 및 관리
- 📎 **첨부파일 관리**: 파일 업로드/다운로드 및 저장소 관리
- 💬 **커뮤니케이션**: 파일 전송 및 커뮤니케이션 유틸리티

## 아키텍처

### 마이크로서비스 구조

```
hermes/
├── 인프라 서비스
│   ├── config-server       # Spring Cloud Config Server (중앙 설정 관리)
│   ├── discovery-server    # Eureka 서비스 디스커버리
│   └── gateway-server      # Spring Cloud Gateway (API 라우팅 및 인증)
│
├── 비즈니스 서비스
│   ├── user-service        # 사용자 인증, 인가 및 관리
│   ├── org-service         # 조직 계층 구조 및 직원 배치 관리
│   ├── attendance-service  # 직원 근태 추적 및 근무 시간 관리
│   ├── approval-service    # 문서 결재 워크플로우 및 템플릿 관리
│   ├── companyinfo-service # 기업 정보 및 설정 관리
│   ├── news-crawler-service # 뉴스 기사 크롤링 및 관리
│   ├── attachment-service  # 첨부파일 저장 및 관리
│   ├── communication-service # 파일 전송 및 커뮤니케이션 유틸리티
│   └── tenant-service      # 멀티테넌트 관리 및 스키마 작업
│
└── 공유 라이브러리
    ├── auth-starter        # JWT 인증/인가 자동 설정
    ├── mt-starter          # 멀티테넌시 자동 설정 (RabbitMQ 이벤트 기반)
    ├── attachment-client-starter # 첨부파일 서비스 통합 클라이언트
    ├── notification-starter # RabbitMQ 기반 비동기 알림 시스템
    ├── events              # 서비스 간 통신을 위한 이벤트 모델
    └── api-common          # 공통 API 유틸리티 (Deprecated)
```

### 기술 스택

- **프레임워크**: Spring Boot 3.5.4, Spring Cloud 2025.0.0
- **언어**: Java 17
- **인증**: JWT (JSON Web Token)
- **서비스 디스커버리**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **메시징**: RabbitMQ
- **데이터베이스**: Schema-per-tenant 전략을 사용한 멀티테넌트 구조
- **문서화**: SpringDoc OpenAPI (Swagger)
- **컨테이너**: Docker, Docker Compose

## 시작하기

### 필수 요구사항

- Java 17 이상
- Docker & Docker Compose (선택사항)
- RabbitMQ (멀티테넌시 및 알림 기능 사용 시)
- PostgreSQL 또는 MySQL (데이터베이스)

### 환경 설정

1. 저장소 복제:
```bash
git clone https://github.com/your-org/hermes-server.git
cd hermes-server
```

2. 환경 변수 설정:
```bash
cp .env.example .env
# .env 파일을 편집하여 필요한 환경 변수 설정
```

3. 빌드:
```bash
# 전체 프로젝트 빌드
./gradlew build -Dfile.encoding=UTF-8

# 특정 서비스만 빌드
./gradlew :user-service:build -Dfile.encoding=UTF-8
```

### 실행 방법

#### Docker Compose 사용 (권장)

```bash
docker-compose up -d
```

#### 로컬 실행

1. Config Server 시작:
```bash
./gradlew :config-server:bootRun
```

2. Discovery Server 시작:
```bash
./gradlew :discovery-server:bootRun
```

3. Gateway Server 및 비즈니스 서비스 시작:
```bash
./gradlew :gateway-server:bootRun
./gradlew :user-service:bootRun
# 필요한 다른 서비스들...
```

### 접근 경로

- **Gateway**: https://localhost:443
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672
- **API 문서**: https://localhost:443/swagger-ui.html

## 개발 가이드

### 빌드 및 테스트

```bash
# 전체 프로젝트 빌드
./gradlew build -Dfile.encoding=UTF-8

# 특정 서비스 빌드
./gradlew :user-service:build -Dfile.encoding=UTF-8

# 전체 테스트 실행
./gradlew test

# 특정 서비스 테스트
./gradlew :user-service:test

# 클린 빌드
./gradlew clean build -Dfile.encoding=UTF-8
```

### 코딩 규칙

- **코드 중복 방지**: 동일한 로직이 반복될 때는 반드시 리팩토링
- **DTO**: Record 클래스 사용 권장
- **시간 처리**: `LocalDateTime`보다 `Instant` 사용 권장
- **Feign Client**: try-catch보다 fallback 사용 권장
- **커밋 메시지**: 한국어로 짧고 간결하게 작성

### 새 서비스 추가하기

1. **의존성 설정**:
```gradle
// build.gradle
dependencies {
    implementation project(':libs:auth-starter')          // 인증/인가
    implementation project(':libs:mt-starter')            // 멀티테넌시
    implementation project(':libs:notification-starter')  // 알림
}
```

2. **보안 설정**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {
    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>
            .AuthorizationManagerRequestMatcherRegistry authz) {
        authz.requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}
```

3. **Swagger 문서화**:
```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Service Name API",
        description = "서비스 설명",
        version = "1.0.0"
    )
)
public class OpenApiConfig { }
```

### 인증/인가 사용법

```java
// 컨트롤러에서 사용자 정보 가져오기
@PostMapping("/documents")
public ResponseEntity<DocumentResponse> createDocument(
    @AuthenticationPrincipal UserPrincipal user,
    @RequestBody CreateDocumentRequest request
) {
    Long userId = user.getId();
    boolean isAdmin = user.isAdmin();
    // 비즈니스 로직...
}

// 메서드 레벨 보안
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ResponseEntity<List<UserResponse>> getAllUsers() {
    // 관리자 전용 엔드포인트
}
```

## 상세 문서

각 공유 라이브러리의 상세한 사용법은 아래 문서를 참고하세요:

- [Auth Starter](libs/auth-starter/README.md) - 인증/인가 설정
- [MT Starter](libs/mt-starter/README.md) - 멀티테넌시 설정
- [Attachment Client Starter](libs/attachment-client-starter/README.md) - 첨부파일 서비스 통합
- [Notification Starter](libs/notification-starter/README.md) - 알림 시스템 설정
