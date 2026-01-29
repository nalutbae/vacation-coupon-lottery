# 키다리스튜디오 백엔드 개발자 기술 과제 회고

## 과제 개요

백엔드 개발자로서 키다리스튜디오의 "휴가 쿠폰 추첨 시스템" 기술 과제를 진행했습니다. 사원들이 응모 코인을 획득하여 1일권/3일권 휴가 쿠폰에 응모하고, 공정한 추첨을 통해 당첨자를 선정하는 시스템을 SpringBoot 3.3 기반으로 구현했습니다.

## 재미있었던 점들

### 1. QueryDSL로 타입 세이프한 쿼리 작성
```java
public List<VacationCouponEntry> findActiveEntriesByPhoneNumber(String phoneNumber) {
    return queryFactory
        .selectFrom(vacationCouponEntry)
        .join(vacationCouponEntry.user, user)
        .where(user.phoneNumber.eq(phoneNumber)
            .and(vacationCouponEntry.isActive.isTrue()))
        .orderBy(vacationCouponEntry.entryTime.desc())
        .fetch();
}
```

컴파일 타임에 쿼리 오류를 잡을 수 있는 QueryDSL을 좋아해 적용했습니다. IDE의 자동완성 기능과 리팩토링 지원까지 받으니 개발 생산성이 확실히 올라갔습니다.

### 2. Liquibase SQL 기반 마이그레이션
```sql
-- 01-create-users-table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(13) NOT NULL UNIQUE,
    coin_count INTEGER NOT NULL DEFAULT 0,
    total_acquired_coins INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

JPA 기능으로 DB를 초기화하거나 직접 SQL을 실행할 수도 있지만, 마이그레이션 버전 관리를 하면서 사람의 실수를 줄일 수 있는 Liquibase를 사용했습니다. XML이나 YAML로 사용해도 되는데 순수 SQL이 명시적이고 개발자로서 편해 선택했습니다. 복잡한 인덱스 생성이나 데이터 마이그레이션도 익숙한 SQL로 직관적으로 처리할 수 있어서 좋았네요.

### 3. Fisher-Yates 셔플 알고리즘을 이용한 공정한 추첨 구현
```java
private void shuffleLotteryPool(List<VacationCouponEntry> lotteryPool) {
    // Fisher-Yates 셔플 알고리즘
    for (int i = lotteryPool.size() - 1; i > 0; i--) {
        int j = secureRandom.nextInt(i + 1);
        
        // 요소 교환
        VacationCouponEntry temp = lotteryPool.get(i);
        lotteryPool.set(i, lotteryPool.get(j));
        lotteryPool.set(j, temp);
    }
}
```

무작위 추첨 기능에 공정성을 더하기 위해 흔히 사용되는 알고리즘을 찾아보고 적용했습니다. 요소 교환 부분의 코드를 `Collections.swap(lotteryPool, i, j);`로 단순화 할 수 있고, 이미 내부적으로 Fisher-Yates 알고리즘이 구현된 `Collections.shuffle(lotteryPool, secureRandom);`을 사용해 더 간결하게 할 수 있다는 것도 새로 공부하는 기회가 되었습니다.

### 4. Docker 멀티 스테이지 빌드

요구사항에는 없었지만 코드를 받아 테스트하는 사람의 입장에서 간편하게 실행할 수 있는 방법이 뭐가 있을지 생각하다 `Docker Compose`가 좋을 것 같아 이미지를 만들어 제공했습니다. 혹시 컨테이너 기반으로 서비스된다면 바로 적용할 수 있을 것 같네요. 멀티 스테이지 빌드로 이미지 크기를 줄이고, `start.sh`, `start.bat` 스크립트까지 만들어서 진짜 원클릭 실행이 가능하도록 했습니다.

## 어렵거나 신경쓰였던 점들

### 1. 동시성 처리의 미묘한 함정
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public ApiResponse<CoinAcquisitionResult> acquireCoin(String phoneNumber) {
    // 900개 한정, 개인당 최대 3개 제한을 동시에 만족시키는 로직
}
```

응모 코인 획득에서 "전체 900개 한정"과 "개인당 최대 3개" 제약을 동시에 만족시키면서 동시성도 보장하는 게 생각보다 까다로웠습니다. SERIALIZABLE 격리 수준을 써야 하나, 낙관적 락을 써야 하나... 성능과 정확성 사이에서 고민이 많았네요. 결국 충돌을 확실히 방지하기 위해 비관적 락과 원자적 업데이트 조합을 선택했지만, 더 아름다운 방법이 있을 것 같아서 아쉽습니다.

### 2. Swagger 어노테이션 설정
```java
@Operation(
        summary = "응모 코인 획득",
        description = "선착순으로 응모 코인 1개를 제공합니다. 일일 한도와 전체 한도가 적용됩니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "코인 획득 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "코인을 성공적으로 획득했습니다",
                        "time": "2026-01-29T10:30:00",
                        "data": {
                            "phoneNumber": "010-1234-5678",
                            "coinCount": 3,
                            "acquisitionTime": "2024-01-29T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "코인 획득 실패 (한도 초과, 수량 부족 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "COIN_LIMIT_EXCEEDED",
                        "message": "일일 코인 획득 한도를 초과했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/acquire")
    public ResponseEntity<ApiResponse<CoinAcquisitionResult>> acquireCoin()...
```
API 문서를 동적으로 제공하기 위해 OpenAPI와 Swagger-UI를 사용했습니다. 친절한 문서 제작을 위해 위와 같이 설정했으나 controller 코드가 길어지고 지저분해진다고 생각해 `@Tag`만을 사용하도록 변경했습니다. 어려운 부분은 아니었지만 API 문서 제공을 좀 더 자세하게 하지 못해 신경쓰입니다. 다른 방식으로 동적 문서를 제공해 보고 싶은 마음이 있습니다. 어찌보면 사소한 환경 설정 이슈지만 협업에 있어 중요하다고 생각합니다.


## 추가로 발전시키고 싶은 점들

### 1. 감사(Audit) 추적 기능
모든 코인 거래를 추적하여 상세하고 투명한 거래 흐름을 추적하는 기능을 추가해 보고 싶습니다. 코인 획득, 차감, 반환, 추첨 등으로 유형을 나누고 거래 간 연관관계 추적 가능이 들어가면 좋을 것 같습니다.

### 2. 분산 환경에서의 동시성 제어
요구사항은 소규모 사용자를 대상으로 하지만 사원 수가 늘어난다면 여러 대의 서버를 두고 분산 환경을 만들 필요가 생기겠지요. 그런 경우를 대비해 Redis와 같은 기술을 활용해 스케일 아웃 환경을 대비하는 기능을 만들어 보고 싶습니다. 핵심적인 부분은 역시 코인 획득 시 전체 개수 한정, 개인당 최대 개수 제약과 동시성 보장을 한번에 만족시키는 지점이 될 것 같습니다.

### 3. 실시간 모니터링 메트릭
현재는 기본적인 로깅만 있는데, 만약 서비스가 확장된다면 Micrometer를 활용한 메트릭 수집, 분산 추적, 구조화된 로깅 등을 도입해서 운영 환경에서의 관측 가능성을 높이고 싶습니다. 여러가지 방법이 있겠지만 Loki와 Grafana를 이용하는 방식을 도입하고 싶습니다.


## 마무리하며

이번 과제를 통해 정말 많은 것을 배웠습니다. 겉보기엔 단순한 "코인 획득 → 응모 → 추첨" 플로우지만, 실제로는 **정산 시스템의 핵심 요구사항들이 축소판으로 담겨있는 종합 문제**였다는 걸 깨달았습니다.

특히 "절대 틀려서는 안 되는 로직"을 구현하는 과정에서 평소 CRUD 개발과는 다른 긴장감을 느꼈습니다. 코인 수량이 음수가 되거나, 동시성 이슈로 중복 지급이 발생하면 안 되는 상황에서 돈을 다루는 마인드가 얼마나 중요한지 체감했습니다.

QueryDSL의 타입 안전성, Liquibase의 마이그레이션 관리, Docker의 배포 편의성 등 개별 기술들도 의미가 있었지만, 가장 값진 경험은 **안정성 있는 비즈니스 로직을 어떻게 안전하게 구현하고 검증할 것인가**에 대한 고민이었습니다.

앞으로 감사 추적, 분산 락, 실시간 모니터링 등을 추가해보고 싶은 이유도 그저 기술적 호기심이 아니라, 실제 서비스 환경에서 마주할 수 있는 복잡성에 대한 준비라고 생각합니다.

개발자로서 가장 보람찬 순간은 복잡한 문제를 우아하게 해결했을 때입니다. 이번 과제는 다시한번 그런 고민을 하게 했기에 의미있었다고 생각하며, 키다리스튜디오에서 더 큰 스케일의 기술적 도전들을 함께 풀어나갈 수 있기를 기대합니다.
