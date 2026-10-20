# 🪐 CraveLog - Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.8-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

<br/>

**취향과 커리어 아카이빙 플랫폼 CraveLog의 고성능·고보안 백엔드 REST API 서버**입니다.  
JWT 무상태 인증, OAuth2 소셜 로그인, 트리 구조 태그 관리, 정밀한 프라이버시 제어, 그리고 비동기 트래픽 분석 파이프라인을 제공합니다.

[🌐 라이브 서비스](https://cravelog.me) · [🖥️ 관리자 콘솔 저장소](../crave-log-admin) · [🎨 프론트엔드 저장소](../crave-log-frontend)

</div>

---

## 📖 목차 (Table of Contents)
- [✨ 프로젝트 개요 (Overview)](#-프로젝트-개요-overview)
- [🎯 핵심 기능 (Key Features)](#-핵심-기능-key-features)
- [🛠️ 기술 스택 (Tech Stack)](#️-기술-스택-tech-stack)
- [🏛️ 시스템 및 보안 아키텍처](#️-시스템-및-보안-아키텍처)
- [📁 패키지 구조 (Package Structure)](#-패키지-구조-package-structure)
- [📡 주요 REST API 명세 (API Endpoints)](#-주요-rest-api-명세-api-endpoints)
- [🚀 시작하기 (Getting Started)](#-시작하기-getting-started)
- [🔐 환경 변수 설정 (Configuration)](#-환경-변수-설정-configuration)
- [📊 플랫폼 트래픽 & 관리자 통계 시스템](#-플랫폼-트래픽--관리자-통계-시스템)
- [🐳 Docker 및 배포 (Deployment)](#-docker-및-배포-deployment)

---

## ✨ 프로젝트 개요 (Overview)

**CraveLog Backend**는 사용자의 직무 이력(Developer, Career)과 개인 취향(Idol, Favorites)을 안전하게 저장하고, 프라이버시 룰과 권한에 따라 최적화된 데이터를 프론트엔드에 서빙하는 백엔드 코어 시스템입니다.

- **무상태(Stateless) JWT & OAuth2 하이브리드 인증**: 카카오 간편 로그인 및 이메일 2-Step 인증 지원
- **트리 기반 계층형 태그 & 아카이빙**: 카테고리-태그-기록 간 N:M 연관관계 최적화
- **정밀 프라이버시 & 암호(Passcode) 잠금**: 탭별 공개 범위 동적 필터링 및 단방향 암호화 검증
- **비동기 트래픽 분석 & 관리자 대시보드 API**: Redis 중복 제거 + DB Atomic Update 기반의 실시간 통계 수집

---

## 🎯 핵심 기능 (Key Features)

### 1. 🔐 보안 & 하이브리드 인증 시스템
- **JWT 무상태 토큰 인증**: Access Token 기반 인증과 `SecurityContextHolder` 연동
- **OAuth2 소셜 연동**: 카카오 간편 로그인 및 회원가입 시 자동 계정 연동
- **이메일 인증코드 발송 (`EmailService`)**: Google SMTP 기반 6자리 난수 인증코드 발송 및 시간 제한 검증
- **비밀번호 단방향 암호화**: `BCryptPasswordEncoder` 기반의 안전한 해싱

### 2. 👤 프로필 & 페르소나 및 팔로우 시스템
- **동적 프로필 조회**: 핸들(`@handle`) 기반 공개 프로필 서빙 및 방문자 수치 누적
- **카테고리별 프라이버시 컨트롤**: `PUBLIC`, `PRIVATE`, `PASSCODE` 3단계 권한 제어
- **암호(Passcode) 검증 API**: 특정 비공개 탭의 비밀번호 일치 여부 판별 후 세션 토큰 부여
- **소셜 팔로우 네트워크**: 유저 간 팔로우/언팔로우 및 양방향 맞팔로우 상태 판별

### 3. 🗂️ 기록(Record) & 계층형 태그 시스템
- **타임라인 & 아카이브 뷰 데이터 최적화**: 날짜/카테고리/태그 기반 고속 인덱싱 쿼리
- **통합 검색 최적화**: MySQL LIKE 검색 시 와일드카드 특수문자(`%`, `_`) 자동 이스케이프 및 빈 쿼리 방어
- **다중 이미지 업로드**: 다중 파일 유효성 검사, UUID 기반 파일명 난수화 및 정적 리소스 서빙

### 4. 📈 고성능 플랫폼 분석 & 관리자 통계
- **비동기 비콘 수집 (`@Async`)**: 사용자 요청 스레드 블로킹 없는 0ms 트래픽 기록
- **Redis 기반 UV 중복 방지**: 당일 자정(23:59:59) 만료 TTL을 적용한 일자별 순 방문자 정밀 집계
- **원자적 카운트 증가 (Atomic Update)**: Lost Update 및 동시 생성 유니크 충돌 원천 차단
- **이중 방어 관리자 API**: IP 화이트리스트(`app.admin.whitelisted-ips`) + `ROLE_ADMIN` 인가
- **로그 자동 정리 스케줄러**: 90일 지난 접속 상세 로그 매일 새벽 3시 자동 퍼지(Purge)

---

## 🛠️ 기술 스택 (Tech Stack)

| 구분 | 기술 / 라이브러리 | 버전 | 설명 |
| :--- | :--- | :--- | :--- |
| **Language** | **Java** | `21` | 최신 LTS 자바 문법 및 가상 스레드 친화적 환경 |
| **Framework** | **Spring Boot** | `3.5.8` | 스프링 부트 3 최신 코어 웹 프레임워크 |
| **Security** | **Spring Security** | `6.x` | 필터 체인 기반 인가, OAuth2 Client, CORS 구성 |
| **Database** | **MySQL** | `8.0` | 메인 RDBMS 영속성 저장소 |
| **In-Memory Cache** | **Redis** | `7.x` | 당일 UV 중복 체크 및 비콘 캐싱 |
| **ORM** | **Spring Data JPA** | - | Hibernate 6 기반 객체-관계 매핑 |
| **Optimization** | **Hypersistence Utils** | `3.7.0` | 고성능 Hibernate 타입 확장 및 JSON 매핑 |
| **Auth Token** | **JJWT** | `0.11.5` | 안전한 JWT 토큰 파싱 및 서명 검증 |
| **Documentation** | **SpringDoc OpenAPI** | `2.3.0` | Swagger UI 자동 생성 및 API 문서화 |
| **Build Tool** | **Gradle** | `8.x` | 유연한 빌드 및 멀티 프로파일 자동화 |

---

## 🏛️ 시스템 및 보안 아키텍처

```text
[ Client (Frontend / Admin / Mobile) ]
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Security Filter                    │
│  ├─ CorsFilter (허용 오리진 및 사전 요청 처리)                │
│  ├─ JwtAuthenticationFilter (토큰 파싱 및 인증 주입)         │
│  └─ SecurityFilterChain (URL별 인가 매핑)                   │
└──────────────────────────────┬──────────────────────────────┘
                               │
       ┌───────────────────────┴───────────────────────┐
       ▼                                               ▼
[ Public APIs ]                                 [ Protected APIs ]
 - /auth/** (이메일/OAuth2)                     - /me/** (내 정보/기록)
 - /users/** (공개 프로필)                      - /files/** (업로드)
 - /analytics/track (비콘 수신)                - /admin/** (관리자 통계)
       │                                               │
       ▼                                               ▼
[ Controller & Async Pipeline ]                 [ Role & IP Defense ]
 - PlatformAnalyticsService (@Async)             - ROLE_ADMIN 검증
 - RedisTemplate (자정 TTL Dedup)               - IP Whitelist 판별
       │                                               │
       └───────────────────────┬───────────────────────┘
                               ▼
                    [ Service & Repository ]
                 - Atomic DB Update 쿼리
                 - JPA 영속성 컨텍스트
                               │
                               ▼
                   [ MySQL 8.0 & Redis 7.0 ]
```

---

## 📁 패키지 구조 (Package Structure)

```text
com.cravelog/
├── ApiApplication.java               # 메인 엔트리포인트 (@EnableAsync, @EnableScheduling, @EnableJpaAuditing)
├── api/                              # REST API 컨트롤러 레이어
│   ├── AuthController.java           # 이메일 로그인/회원가입, 인증코드 발송/확인
│   ├── FileController.java           # 이미지 파일 업로드
│   ├── FollowController.java         # 유저 팔로우/언팔로우 및 상태 조회
│   ├── PlatformAnalyticsController.java # 트래픽 비콘 수신 및 관리자 대시보드 API
│   ├── ProfileController.java        # 공개 프로필 및 카테고리/기록 조회
│   └── RecordController.java         # 기록(Record) CRUD, 검색, 암호 검증
├── config/                           # 인프라 및 시큐리티 설정
│   ├── SecurityConfig.java           # CORS, URL 인가, JWT 필터 체인
│   ├── PasswordEncoderConfig.java    # BCrypt 인코더 빈
│   └── WebConfig.java                # 정적 리소스(/uploads/**) 서빙 매핑
├── domain/                           # 비즈니스 도메인 엔티티 및 서비스
│   ├── analytics/                    # 플랫폼 전체 트래픽 통계 도메인
│   │   ├── PlatformDailyStat.java    # 일자별 누적 통계 엔티티 (UV, PV, 기기 카운트)
│   │   ├── PlatformAccessLog.java    # 상세 접속 로그 엔티티 (마스킹 IP, 경로, 브라우저, 체류시간)
│   │   ├── PlatformAnalyticsService.java # 비동기 비콘 기록 및 대시보드 오버뷰 계산
│   │   └── dto/                      # 통계 응답 DTOs
│   ├── record/                       # 기록 도메인
│   │   ├── Record.java               # 메인 아카이빙 기록 엔티티
│   │   ├── RecordTag.java            # 기록-태그 N:M 매핑 엔티티
│   │   └── RecordService.java        # 기록 비즈니스 로직 및 검색
│   ├── tag/                          # 카테고리 & 태그 도메인
│   │   ├── Category.java             # 상위 카테고리 (Developer, Career, Idol 등)
│   │   └── Tag.java                  # 하위 세부 태그
│   └── user/                         # 사용자 및 인증 도메인
│       ├── User.java                 # 유저 엔티티 (역할, 프로필, 프라이버시 맵)
│       ├── Follow.java               # 팔로우 관계 엔티티
│       ├── DailyVisit.java           # 개인 프로필 방문자 엔티티
│       ├── AuthService.java          # 회원가입/로그인 인증 서비스
│       ├── EmailService.java         # 이메일 인증코드 발송
│       ├── VisitService.java         # 개인 프로필 방문 통계 서비스
│       └── UserService.java          # 유저 프로필 및 프라이버시 관리
└── security/                         # JWT & OAuth2 보안 구현체
    ├── JwtAuthenticationFilter.java  # HTTP 헤더 Bearer 토큰 추출 필터
    ├── TokenProvider.java            # JWT 토큰 생성, 검증, Claims 추출
    └── oauth/                        # 카카오 OAuth2 핸들러 및 서비스
```

---

## 📡 주요 REST API 명세 (API Endpoints)

### 1. 인증 (Auth)
| Method | Endpoint | 설명 | 권한 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/email/send-code` | 회원가입용 6자리 이메일 인증코드 발송 | Public |
| `POST` | `/api/v1/auth/email/verify-code`| 인증코드 유효성 검증 | Public |
| `POST` | `/api/v1/auth/signup` | 이메일 기반 회원가입 및 토큰 발급 | Public |
| `POST` | `/api/v1/auth/login` | 이메일/비밀번호 로그인 | Public |

### 2. 프로필 & 기록 (Profile & Records)
| Method | Endpoint | 설명 | 권한 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/users/{handle}/profile` | 공개 프로필 정보 및 카테고리/태그 트리 조회 | Public |
| `GET` | `/api/v1/users/{handle}/visit-stats`| 개인 프로필 일간/주간 방문자 통계 조회 | Public |
| `POST`| `/api/v1/records/*/verify-passcode` | 암호 잠금 탭 접근을 위한 비밀번호 검증 | Public |
| `POST`| `/api/v1/records` | 신규 기록 작성 (이미지, 링크, 태그) | User |
| `PUT` | `/api/v1/records/{id}` | 기존 기록 수정 | Owner |
| `DELETE`| `/api/v1/records/{id}` | 기록 삭제 | Owner |
| `GET` | `/api/v1/records/search` | 유저 및 전체 공개 기록 통합 검색 | Public |

### 3. 소셜 팔로우 (Follow)
| Method | Endpoint | 설명 | 권한 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users/{targetUserId}/follow` | 유저 팔로우 | User |
| `DELETE`| `/api/v1/users/{targetUserId}/follow` | 유저 언팔로우 | User |
| `GET` | `/api/v1/users/{handle}/follow-status`| 특정 사용자와의 팔로우/맞팔 상태 확인 | Public |

### 4. 플랫폼 트래픽 & 관리자 분석 (Analytics & Admin)
| Method | Endpoint | 설명 | 권한 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/analytics/track` | 프론트엔드 라우트 이동/이탈 시 비콘 수집 (`@Async`) | Public |
| `GET` | `/api/v1/admin/analytics/overview` | 통합 트래픽 대시보드 지표 조회 (UV, PV, 점유율 등) | Admin + IP |

---

## 🚀 시작하기 (Getting Started)

### 1. 요구 사항 (Prerequisites)
- **Java**: `JDK 21` 이상 (OpenJDK / Corretto 21)
- **MySQL**: `8.0` 이상
- **Redis**: `7.0` 이상 (로컬 또는 원격)

### 2. 레포지토리 클론 & 빌드
```bash
# 저장소 클론
git clone https://github.com/your-username/crave-log.git
cd crave-log/crave-log-backend

# Gradle 빌드 (테스트 포함)
./gradlew build

# 또는 컴파일 전용 실행
./gradlew compileJava
```

### 3. 로컬 실행
```bash
# 로컬 프로파일(local)로 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```
기본 포트는 `8080`이며, Swagger 문서는 `http://localhost:8080/swagger-ui/index.html`에서 확인할 수 있습니다.

---

## 🔐 환경 변수 설정 (Configuration)

`src/main/resources/application.yaml`에서 참조하는 주요 환경 변수 목록입니다:

```env
# 1. Database (MySQL)
DB_URL=jdbc:mysql://localhost:3306/crave_log?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=root
DB_PASSWORD=your_password

# 2. Redis
REDIS_URL=redis://localhost:6379

# 3. JWT Secret (최소 256비트 이상의 안전한 문자열)
JWT_SECRET=your_super_secret_jwt_key_here_at_least_32_characters_long

# 4. Kakao OAuth2
KAKAO_CLIENT_ID=your_kakao_rest_api_key
KAKAO_CLIENT_SECRET=your_kakao_client_secret

# 5. Google SMTP Mail
MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_PASSWORD=your_google_app_password

# 6. CORS & Redirect
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,https://cravelog.me
FRONTEND_URL=https://cravelog.me

# 7. 관리자 콘솔 허용 IP (*: 모든 IP 허용, 또는 특정 IP / 사설망 대역 지정)
ADMIN_WHITELISTED_IPS=127.0.0.1,::1,0:0:0:0:0:0:0:1,localhost,192.168.,172.,10.
```

---

## 📊 플랫폼 트래픽 & 관리자 통계 시스템

백엔드는 대규모 동시 접속 상황에서도 안정적인 트래픽 수집을 보장하기 위해 다음과 같은 아키텍처를 구현했습니다:

1. **비동기 수집 (`@Async`)**:
   - `/api/v1/analytics/track` 엔드포인트는 비콘을 수신한 즉시 `200 OK`를 클라이언트에 반환하며, 실제 DB/Redis 기록은 백그라운드 스레드 풀에서 비동기로 수행됩니다.
2. **원자적 증가 (Atomic Update Query)**:
   - 메모리에서 엔티티를 조회하여 수정하는 대신, DB 레벨의 `UPDATE` 쿼리로 동시 다발적인 페이지뷰/방문자 증가를 처리하여 **Lost Update** 문제를 완벽히 방지합니다.
3. **자정 만료 Redis UV 캐시**:
   - `Platform:Dedup:{YYYY-MM-DD}:{ClientIP}` 키를 생성하고 당일 23:59:59까지의 남은 시간으로 만료 TTL을 동적 설정하여 익일 통계 오염과 메모리 낭비를 방지합니다.
4. **인덱스 기반 고속 집계**:
   - 최근 `days`일 파라미터에 맞추어 `WHERE l.accessedAt >= :since` 조건과 `(event_type, accessed_at DESC)` 복합 인덱스를 사용하여 Full Table Scan을 방지합니다.

---

## 🐳 Docker 및 배포 (Deployment)

### Dockerfile 빌드 및 실행
```bash
# 1. 도커 이미지 빌드
docker build -t cravelog-backend:latest .

# 2. 컨테이너 구동 (환경 변수 주입)
docker run -d \
  -p 8080:8080 \
  --name cravelog-api \
  --env-file .env.prod \
  cravelog-backend:latest
```

---

## 📄 라이선스 (License)

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
