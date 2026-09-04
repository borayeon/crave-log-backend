# CraveLog - Backend (Spring Boot)

나만의 취향을 아카이빙하는 **CraveLog**의 안정적인 데이터 처리와 인증을 담당하는 백엔드 서버(REST API) 레포지토리입니다.

---

# ✨ 프로젝트 소개 (About)

CraveLog는 사용자별 커스텀 프로필 데이터를 안전하게 저장하고 제공하는 아카이빙 플랫폼입니다.
트리 구조 기반의 태그 시스템과 아카이브 기록을 효율적으로 관리하며, 최적화된 데이터 구조로 프론트엔드에 전달합니다.
또한 강력한 암호화(Argon2 / BCrypt)와 JWT 기반의 무상태(Stateless) 인증 아키텍처를 도입하여 보안성과 확장성을 동시에 확보했습니다.

---

# 🚀 기술 스택 (Tech Stack)

## Backend
- Java 21
- Spring Boot 3.x

## Security
- Spring Security
- OAuth2 (Kakao)
- JWT (JSON Web Token)
- Argon2 / BCrypt

## Database
- MySQL 8.0 (Primary)
- Redis (Token Caching)

## ORM
- Spring Data JPA
- Hibernate

## Infrastructure & Deployment
- Docker & Docker Compose
- Cloudflare Tunnel (내부망 격리 및 HTTPS)
- Ubuntu Home Server (Local / Staging / Prod 환경 완벽 분리)

---

# 🎯 주요 기능 (Key Features)

## 🔐 하이브리드 인증 시스템
- **JWT 기반 Stateless 인증:** Access Token 기반 로그인 유지, Redis를 활용한 Refresh Token 관리
- **OAuth2 소셜 로그인:** 카카오(Kakao) 로그인 연동 지원
- **로컬 인증 시스템:** 이메일 기반 회원가입 및 로그인, 2-Step 인증 UI 연동

## 🌐 사용자 경험 및 RESTful API 설계
- **초기 적응 가이드:** 신규 가입 유저를 위한 튜토리얼 제공 및 상태(isTutorialCompleted) 동기화 API (`PATCH /api/v1/me/tutorial`)
- **게스트 프로필 조회:** `GET /api/v1/users/{handle}/profile` (공개된 프로필 및 기록 조회)
- **마이페이지 조회:** `GET /api/v1/me/records` (비공개 기록을 포함한 전체 데이터 조회)

## 🌳 효율적인 데이터 매핑
`User` - `Category` - `Tag` - `Record` - `RecordTag` 엔티티 간 연관관계를 기반으로 트리 구조 설계
- 계층형 태그 시스템 지원
- DTO 기반 응답 최적화 및 Fetch 전략 개선 (N+1 문제 방어)

## 🔎 검색 쿼리 최적화
- MySQL 특수문자 이스케이프 처리 및 빈 검색어(Empty Query) 예외 처리
- 전체 목록 안전 반환 및 검색 성능 최적화

## 🔒 동적 프로필 공개 범위 설정
사용자가 설정한 공개 범위를 DB에 저장하고 응답 시 동적으로 필터링 (Developer, Career, Idol, Favorites 등)
- Privacy Map(JSON)을 파싱하여 사용자 권한에 따라 데이터 맞춤 제공

---

# 🛠️ 설치 및 환경 설정 (Getting Started)

## 1. 저장소 클론
```bash
git clone [https://github.com/your-username/cravelog-backend.git](https://github.com/your-username/cravelog-backend.git)
cd cravelog-backend
```

## 2. 환경 변수 설정 (`application-local.yml`)
로컬 개발 환경 구동을 위해 `src/main/resources` 디렉토리에 `application-local.yml`을 생성합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cravelog
    username: root
    password: password
  data:
    redis:
      host: localhost
      port: 6379
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: your-secret-key

app:
  oauth2:
    authorized-redirect-uri: http://localhost:5173/oauth2/redirect
```

## 3. 애플리케이션 실행 (Local Profile)
```bash
# Gradle 실행 (Local 환경 활성화)
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

# 🐳 배포 환경 (Deployment)

CraveLog는 안전한 테스트와 운영을 위해 데이터베이스와 백엔드 컨테이너가 분리된 아키텍처를 사용합니다.

```bash
# 1. 애플리케이션 빌드
./gradlew clean build

# 2. 도커 이미지 빌드
docker build -t your-docker-id/cravelog-backend:prod .

# 3. 도커 컴포즈 실행 (DB, Redis, Backend, Cloudflared)
docker compose up -d
```

---

# 🔒 보안 아키텍처

```text
Client
   ↓
JWT Access Token (or OAuth2 Login)
   ↓
Spring Security Filter
   ↓
JwtAuthenticationFilter (Redis Refresh Token 검증)
   ↓
SecurityContext
   ↓
Controller → Service → Database
```

---

# 📄 License

본 프로젝트는 개인 포트폴리오 및 학습 목적으로 제작되었습니다.
