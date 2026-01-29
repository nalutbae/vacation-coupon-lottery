# 휴가 쿠폰 추첨 시스템

키다리스튜디오 사원 대상 분기별 휴가 쿠폰 추첨 시스템입니다.

## 📋 추가 문서

- **[📊 DDL 및 ERD](DDL.md)** - 데이터베이스 스키마 및 ERD 다이어그램
- **[📝 개발 회고](Retrospective.md)** - 기술 과제 진행 회고 및 인사이트

## 🛠️ 기술 스택

- **Framework**: SpringBoot 3.3
- **Database**: H2 (In-Memory/File)
- **ORM**: JPA + QueryDSL
- **Migration**: Liquibase (SQL 기반)
- **Build Tool**: Gradle 8.7
- **Java Version**: 17
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

**방법 1: 시작 스크립트 사용 (가장 쉬운 방법)**
```bash
# Linux/Mac 사용자
./start.sh

# Windows 사용자
start.bat
```

**방법 2: 직접 명령어 실행**
```bash
# 프로젝트 디렉토리에서 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down
```

#### 3. Docker 직접 사용
```bash
# 1. Docker 이미지 빌드
docker build -t vacation-coupon-lottery .

# 2. 컨테이너 실행
docker run -d \
  --name vacation-coupon-lottery \
  -p 8080:8080 \
  -v $(pwd)/data:/app/data \
  vacation-coupon-lottery

# 3. 로그 확인
docker logs -f vacation-coupon-lottery

# 4. 컨테이너 중지 및 제거
docker stop vacation-coupon-lottery
docker rm vacation-coupon-lottery
```

#### 4. 브라우저에서 확인
애플리케이션이 시작되면 다음 URL들을 브라우저에서 바로 확인할 수 있습니다:

- **🏠 메인 페이지**: http://localhost:8080
- **📋 API 문서 (Swagger)**: http://localhost:8080/swagger-ui/index.html
- **🎯 응모 페이지**: http://localhost:8080/enter
- **🎲 추첨 관리**: http://localhost:8080/draw
- **🏆 당첨자 조회**: http://localhost:8080/winner
- **💾 데이터베이스 콘솔**: http://localhost:8080/h2-console (개발 모드에서만)

#### 5. 데이터 영속성
- Docker 실행 시 `./data` 디렉토리에 데이터베이스 파일이 저장됩니다
- 컨테이너를 재시작해도 데이터가 유지됩니다

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

## 🎯 주요 기능

1. **응모 코인 관리**: 선착순 900개 한정, 개인당 최대 3개
2. **휴가 쿠폰 응모**: 1일권/3일권 자유 선택, 응모 취소 가능
3. **공정한 추첨**: Fisher-Yates 셔플 알고리즘 기반
4. **현황 조회**: 개인/전체 응모 현황 실시간 조회
5. **웹 인터페이스**: 간단한 웹 페이지 제공
6. **API 문서화**: Swagger/OpenAPI 3.0 기반 자동 문서 생성


## FAQ

### Q: Docker를 처음 사용하는데 어떻게 설치하나요?
A: [Docker 공식 사이트](https://www.docker.com/get-started)에서 운영체제에 맞는 Docker Desktop을 다운로드하여 설치하세요.

### Q: 포트 8080이 이미 사용 중이라고 나와요.
A: `docker-compose.yml` 파일에서 `"8080:8080"`을 `"9090:8080"`으로 변경하고 http://localhost:9090으로 접속하세요.

### Q: 데이터가 사라지지 않나요?
A: `./data` 디렉토리에 데이터베이스 파일이 저장되므로 컨테이너를 재시작해도 데이터가 유지됩니다.

### Q: 메모리 사용량을 줄이고 싶어요.
A: `docker-compose.yml`의 `JAVA_OPTS`에서 `-Xmx512m`을 `-Xmx256m`으로 변경하세요.

### Q: 애플리케이션이 시작되지 않아요.
A: 다음 명령어로 로그를 확인하세요:
```bash
docker-compose logs -f
```

### Q: 완전히 초기화하고 싶어요.
A: 다음 명령어를 실행하세요:
```bash
docker-compose down --rmi all
rm -rf data/
docker-compose up -d
```

## 📞 지원 및 문의

- **개발자**: 정태현
- **이메일**: chomman@naver.com
- **포트폴리오**: https://hyun-portfolio.vercel.app/