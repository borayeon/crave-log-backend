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

- MySQL (Aiven)
- H2 Database (Local Test)

## ORM

- Spring Data JPA

## Build Tool

- Gradle

## Deployment

- Docker
- Render

---

# 🎯 주요 기능 (Key Features)

## 🔐 하이브리드 인증 시스템

### JWT 기반 Stateless 인증

- Access Token 기반 로그인 유지
- 서버 세션 저장 없이 인증 처리

### OAuth2 소셜 로그인

- 카카오 로그인 지원

### 로컬 인증 시스템

- 이메일 기반 회원가입 및 로그인
- 2-Step 인증 UI 연동

---

## 🌐 RESTful API 설계

### 게스트 프로필 조회

```http
GET /api/v1/users/{handle}/profile
```

공개된 프로필 및 기록을 조회합니다.

### 마이페이지 조회

```http
GET /api/v1/me/records
```

비공개 기록을 포함한 전체 데이터를 조회합니다.

---

## 🌳 효율적인 데이터 매핑

다음 엔티티 간 연관관계를 기반으로 트리 구조를 설계했습니다.

- User
- Category
- Tag
- Record

### 설계 목표

- 계층형 태그 시스템 지원
- DTO 기반 응답 최적화
- Fetch 전략 개선
- N+1 문제 방어

---

## 🔎 검색 쿼리 최적화

- MySQL 특수문자 이스케이프 처리
- 빈 검색어(Empty Query) 예외 처리
- 전체 목록 안전 반환
- 검색 성능 최적화

---

## 🔒 동적 프로필 공개 범위 설정

사용자가 설정한 공개 범위를 DB에 저장하고 응답 시 동적으로 필터링합니다.

### 지원 범위

- Developer
- Career
- Idol
- Favorites

Privacy Map(JSON)을 파싱하여 사용자 권한에 따라 데이터를 제공합니다.

---

# 📁 주요 패키지 구조 (Directory Structure)

```text
src/main/java/com/cravelog/
├── api/
│   └── REST API Controller
│       ├── AuthController
│       ├── ProfileController
│       └── UserController
│
├── config/
│   ├── SecurityConfig
│   ├── WebConfig
│   └── DataInitializer
│
├── domain/
│   ├── record/
│   │   ├── Record
│   │   ├── RecordTag
│   │   ├── DTO
│   │   └── Service
│   │
│   ├── tag/
│   │   ├── Category
│   │   ├── Tag
│   │   └── DTO
│   │
│   └── user/
│       ├── User
│       ├── UserService
│       └── Repository
│
└── security/
    ├── JwtAuthenticationFilter
    ├── TokenProvider
    ├── OAuth2SuccessHandler
    └── CustomOAuth2UserService
```

---

# 🛠️ 설치 및 환경 설정 (Getting Started)

## 1. 저장소 클론

```bash
git clone https://github.com/your-username/cravelog-backend.git

cd cravelog-backend
```

---

## 2. 환경 변수 설정

배포 및 로컬 실행을 위해 아래 환경 변수를 등록해야 합니다.

### JWT

```env
JWT_SECRET=your-secret-key
```

### Kakao OAuth

```env
KAKAO_CLIENT_ID=your-client-id

KAKAO_CLIENT_SECRET=your-client-secret
```

### Database

```env
DB_URL=jdbc:mysql://localhost:3306/cravelog

DB_USERNAME=root

DB_PASSWORD=password
```

---

## 3. application.yml 예시

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: ${JWT_SECRET}
```

---

## 4. 애플리케이션 실행

### Gradle 실행

```bash
./gradlew bootRun
```

### 빌드

```bash
./gradlew build
```

---

# 🐳 Docker 실행

## 이미지 빌드

```bash
docker build -t cravelog-backend .
```

## 컨테이너 실행

```bash
docker run -p 8080:8080 cravelog-backend
```

---

# 🗄️ 데이터베이스 구조

### 핵심 엔티티

- User
- Record
- Category
- Tag
- RecordTag
- Follow
- Privacy

---

# 🔒 보안 아키텍처

```text
Client
   ↓
JWT Access Token
   ↓
Spring Security Filter
   ↓
JwtAuthenticationFilter
   ↓
SecurityContext
   ↓
Controller
   ↓
Service
   ↓
Database
```

---

# 🌍 배포 환경

| 서비스 | 플랫폼 |
| --- | --- |
| Backend API | Render |
| Database | Aiven MySQL |
| Container | Docker |

---

# 📄 License

본 프로젝트는 개인 포트폴리오 및 학습 목적으로 제작되었습니다.
