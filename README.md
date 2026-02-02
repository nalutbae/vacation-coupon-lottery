# 휴가 쿠폰 추첨 시스템

키다리스튜디오 사원 대상 분기별 휴가 쿠폰 추첨 시스템입니다.

## 📋 추가 문서

- **[📊 DDL 및 ERD](DDL.md)** - 데이터베이스 스키마 및 ERD 다이어그램
- **[📝 개발 회고](Retrospective.md)** - 기술 과제 진행 회고

## 🛠️ 기술 스택

- **Java Version**: 17
- **Framework**: SpringBoot 3.3
- **Database**: H2 (In-Memory/File)
- **ORM**: JPA + QueryDSL
- **DB Migration**: Liquibase (SQL 기반)
- **Build Tool**: Gradle 8.7
- **Code Generation**: Lombok
- **Testing**: JUnit 5
- **API Documentation**: SpringDoc OpenAPI 3.0
- **Containerization**: Docker + Docker Compose

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/kidaristudio/vacationcouponlottery/
│   │   ├── VacationCouponLotteryApplication.java
│   │   ├── config/                    # 설정 클래스
│   │   │   ├── JpaConfig.java
│   │   │   └── SwaggerConfig.java
│   │   ├── controller/                # REST API 컨트롤러
│   │   ├── service/                   # 비즈니스 로직
│   │   ├── repository/                # 데이터 접근 계층 (QueryDSL)
│   │   ├── domain/                    # JPA 엔티티
│   │   ├── dto/                       # 데이터 전송 객체
│   │   └── exception/                 # 예외 처리
│   └── resources/
│       ├── application.yaml           # 환경별 설정
│       ├── messages.properties        # 다국어 메시지
│       ├── templates/                 # Thymeleaf 웹 페이지
│       └── db/changelog/              # Liquibase 마이그레이션
│           ├── db.changelog-master.yaml
│           └── v1.0/
│               ├── 01-create-users-table.sql
│               ├── 02-create-vacation-coupon-entries-table.sql
│               ├── 03-create-system-config-table.sql
│               ├── 04-create-lottery-results-table.sql
│               └── 05-insert-initial-system-config.sql
└── test/
    └── java/com/kidaristudio/vacationcouponlottery/
        ├── controller/                # API 테스트
        ├── service/                   # 비즈니스 로직 테스트
        └── integration/               # 통합 테스트
```

## 💾 데이터베이스 설정

### 개발 환경 (dev 프로필)
- H2 인메모리 데이터베이스
- H2 콘솔 활성화: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:vacation_lottery_dev`

### 운영 환경 (prod 프로필)
- H2 파일 데이터베이스
- 데이터 파일 위치: `./data/vacation_lottery_prod`
- H2 콘솔 비활성화

> 📊 **상세한 데이터베이스 스키마는 [DDL.md](DDL.md)를 참조하세요.**

## 실행 방법

### 🐳 Docker로 실행 (권장)

**Java나 Gradle을 설치하지 않고도 Docker만으로 바로 실행할 수 있습니다.**

#### 1. 사전 요구사항
- [Docker](https://www.docker.com/get-started) 설치
- [Docker Compose](https://docs.docker.com/compose/install/) 설치 (Docker Desktop에 포함)

#### 2. 간단한 실행 (Docker Compose 사용)

```bash
# 프로젝트 디렉토리에서 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down

# 완전히 초기화하고 다시 실행하는 방법
docker-compose down --rmi all
rm -rf data/
docker-compose up -d
```

#### 3. 브라우저에서 확인
애플리케이션이 시작되면 다음 URL들을 브라우저에서 바로 확인할 수 있습니다:

- **🏠 메인 페이지**: http://localhost:8080
- **📋 API 문서 (Swagger)**: http://localhost:8080/swagger-ui/index.html
- **🎯 응모 페이지**: http://localhost:8080/enter
- **🎲 추첨 관리**: http://localhost:8080/draw
- **🏆 당첨자 조회**: http://localhost:8080/winner
- **💾 데이터베이스 콘솔**: http://localhost:8080/h2-console (개발 모드에서만)

#### 4. 데이터 영속성
- Docker 실행 시 `./data` 디렉토리에 데이터베이스 파일이 저장됩니다.
- 컨테이너를 재시작해도 데이터가 유지됩니다.
- 데이터를 초기화하려면 `/data` 내 파일을 제거하고 다시 실행하세요.

### 💻 로컬 개발 환경에서 실행

#### 사전 요구사항
- Java 17 이상
- Gradle 8.7 이상 (또는 Gradle Wrapper 사용)

#### 개발 환경에서 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### 운영 환경에서 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

#### 테스트 실행
```bash
./gradlew test
```

## API 문서

이 프로젝트는 **Swagger/OpenAPI 3.0**을 사용하여 API 문서를 자동 생성합니다.

### Swagger UI 접근
애플리케이션 실행 후 다음 URL에서 대화형 API 문서를 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

### OpenAPI JSON
API 스펙을 JSON 형태로 확인하려면:
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### API 그룹

API는 다음과 같이 4개 그룹으로 구성됩니다:

1. **응모 코인 관리** (`/api/coins`)
   - 코인 획득, 수량 조회, 전체 현황 조회

2. **휴가 쿠폰 응모** (`/api/coupons`)
   - 응모 등록, 취소, 개인 현황 조회

3. **추첨 시스템** (`/api/lottery`)
   - 추첨 실행, 당첨자 조회, 추첨 상태 확인

4. **현황 조회** (`/api/status`)
   - 개인/전체 통계, 시스템 현황 조회


## 시스템 설정

시스템 초기 설정값:
- 전체 응모 코인 수량: 900개
- 사용자당 최대 코인 수: 3개
- 쿠폰당 당첨자 수: 3명


## 📞 지원 및 문의

- **개발자**: 정태현
- **이메일**: chomman@naver.com
- **포트폴리오**: https://hyun-portfolio.vercel.app/