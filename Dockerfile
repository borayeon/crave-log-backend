# Java 21 환경 (알파인 리눅스로 용량 최소화)
FROM eclipse-temurin:21-jre-alpine

# 컨테이너 내 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 파일을 컨테이너의 app.jar로 복사
# Spring Boot 3은 plain.jar도 생성하므로, 기본 jar만 복사하도록 설정
COPY build/libs/*SNAPSHOT.jar app.jar

# 애플리케이션 포트
EXPOSE 8080

# ⭐️ 컨테이너 실행 시 작동할 명령어 (prod 프로필 명시!)
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]