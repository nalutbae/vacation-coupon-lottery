# 휴가 쿠폰 추첨 시스템 Docker 이미지
# Java 17 기반 SpringBoot 애플리케이션

# 1단계: 빌드 환경
FROM gradle:8.7-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 파일 복사
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# 2단계: 실행 환경
FROM eclipse-temurin:17-jre-jammy

# 필요한 도구 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 메타데이터 설정
LABEL maintainer="정태현 <chomman@naver.com>"
LABEL description="키다리스튜디오 휴가 쿠폰 추첨 시스템"
LABEL version="1.0.0"

# 작업 디렉토리 설정
WORKDIR /app

# 데이터 디렉토리 생성 (H2 파일 데이터베이스용)
RUN mkdir -p /app/data

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크 설정 (메인 페이지 확인)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# 애플리케이션 실행
# 운영 환경 프로필로 실행하여 데이터 영속성 보장
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]