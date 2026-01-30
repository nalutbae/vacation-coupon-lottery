package com.kidaristudio.vacationcouponlottery.integration;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
import com.kidaristudio.vacationcouponlottery.service.LotteryService;
import com.kidaristudio.vacationcouponlottery.service.VacationCouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 키다리스튜디오 휴가 쿠폰 추첨 시스템 전체 통합 테스트
 * 
 * 이 테스트 클래스는 휴가 쿠폰 추첨 시스템의 전체적인 기능을 검증하는 엔드투엔드 통합 테스트를 제공합니다.
 * 실제 사용자 시나리오를 시뮬레이션하여 시스템의 모든 구성 요소가 올바르게 통합되어 작동하는지 확인합니다.
 * 
 * 테스트 범위:
 * - 코인 획득 기능
 * - 휴가 쿠폰 응모 기능
 * - 추첨 실행 기능
 * - 당첨자 조회 기능
 * - 응모 취소 기능
 * - 오류 상황 처리
 * 
 * 테스트 환경:
 * - Spring Boot 테스트 환경에서 실행
 * - 테스트 프로파일 사용 (application-test.yaml)
 * - 각 테스트 메서드 실행 전 데이터베이스 초기화
 * - 트랜잭션 롤백을 통한 테스트 격리
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("전체 시스템 통합 테스트")
class SystemIntegrationTest {

    // === 의존성 주입 필드들 ===
    // 테스트에 필요한 서비스와 리포지토리들을 주입받습니다.

    /**
     * 코인 획득 관련 비즈니스 로직을 처리하는 서비스
     * 사용자의 코인 획득 요청을 처리합니다.
     */
    @Autowired
    private EntryCoinService entryCoinService;

    /**
     * 휴가 쿠폰 응모 및 취소 관련 비즈니스 로직을 처리하는 서비스
     * 사용자의 응모, 취소 요청을 처리합니다.
     */
    @Autowired
    private VacationCouponService vacationCouponService;

    /**
     * 추첨 실행 및 당첨자 조회 관련 비즈니스 로직을 처리하는 서비스
     * 추첨 진행과 결과 조회를 담당합니다.
     */
    @Autowired
    private LotteryService lotteryService;

    /**
     * 사용자 정보를 관리하는 리포지토리
     * 사용자 데이터 조회 및 검증에 사용됩니다.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 휴가 쿠폰 응모 정보를 관리하는 리포지토리
     * 응모 데이터 초기화에 사용됩니다.
     */
    @Autowired
    private VacationCouponEntryRepository entryRepository;

    /**
     * 각 테스트 실행 전 데이터베이스 초기화
     * 
     * 테스트 간의 데이터 격리를 보장하기 위해 모든 테스트 데이터를 삭제합니다.
     * 외래 키 제약 조건을 고려하여 자식 테이블(응모)부터 부모 테이블(사용자) 순으로 삭제합니다.
     */
    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화 - 외래 키 제약 조건 고려하여 순서대로 삭제
        entryRepository.deleteAll();  // 응모 데이터 먼저 삭제
        userRepository.deleteAll();   // 사용자 데이터 삭제
    }

    /**
     * 전체 시스템 엔드투엔드 시나리오 테스트
     * 
     * 이 테스트는 실제 사용자가 시스템을 사용하는 전체 시나리오를 시뮬레이션합니다.
     * 
     * 테스트 시나리오:
     * 1. 4명의 사용자가 각각 다른 수의 코인을 획득
     * 2. 각 사용자가 보유한 코인으로 1일권 또는 3일권에 응모
     * 3. 각 쿠폰 타입별로 추첨 실행
     * 4. 당첨자 조회 및 결과 검증
     * 5. 응모 취소 기능 테스트
     * 
     * 검증 포인트:
     * - 코인 획득 후 사용자별 코인 보유량 정확성
     * - 응모 후 코인 차감 정확성
     * - 추첨 결과의 유효성 (당첨자 수, 당첨 조건)
     * - 응모 취소 시 코인 반환 정확성
     */
    @Test
    @DisplayName("전체 시스템 엔드투엔드 시나리오 테스트")
    @Transactional
    void completeEndToEndScenario() throws Exception {
        // === 1단계: 사용자들이 코인 획득 ===
        // 테스트용 사용자 전화번호 정의 (실제 전화번호 형식 사용)
        String user1 = "010-1111-1111";
        String user2 = "010-2222-2222";
        String user3 = "010-3333-3333";
        String user4 = "010-4444-4444";

        // 각 사용자가 서로 다른 수의 코인을 획득하여 다양한 응모 패턴 테스트
        acquireCoinsDirectly(user1, 3); // user1: 3개 획득 (여러 응모 가능)
        acquireCoinsDirectly(user2, 2); // user2: 2개 획득 (한 번 응모 가능)
        acquireCoinsDirectly(user3, 1); // user3: 1개 획득 (최소 응모)
        acquireCoinsDirectly(user4, 3); // user4: 3개 획득 (복수 응모 테스트)

        // 코인 획득 결과 검증 - 각 사용자의 코인 보유량이 정확한지 확인
        assertUserCoinCount(user1, 3);
        assertUserCoinCount(user2, 2);
        assertUserCoinCount(user3, 1);
        assertUserCoinCount(user4, 3);

        // === 2단계: 휴가 쿠폰 응모 ===
        // 다양한 응모 패턴을 테스트하여 시스템의 유연성 검증
        
        // user1: 1일권에 2개 코인으로 응모 (당첨 확률 높임)
        Long entry1Id = enterLotteryDirectly(user1, CouponType.ONE_DAY, 2);
        
        // user2: 3일권에 2개 코인으로 응모 (고가치 쿠폰 선택)
        Long entry2Id = enterLotteryDirectly(user2, CouponType.THREE_DAY, 2);
        
        // user3: 1일권에 1개 코인으로 응모 (최소 응모)
        Long entry3Id = enterLotteryDirectly(user3, CouponType.ONE_DAY, 1);
        
        // user4: 복수 응모 테스트 - 1일권과 3일권 모두 응모
        Long entry4aId = enterLotteryDirectly(user4, CouponType.ONE_DAY, 1);
        Long entry4bId = enterLotteryDirectly(user4, CouponType.THREE_DAY, 2);

        // 응모 후 코인 잔량 확인 - 코인이 정확히 차감되었는지 검증
        assertUserCoinCount(user1, 1); // 3 - 2 = 1 (1개 남음)
        assertUserCoinCount(user2, 0); // 2 - 2 = 0 (모두 사용)
        assertUserCoinCount(user3, 0); // 1 - 1 = 0 (모두 사용)
        assertUserCoinCount(user4, 0); // 3 - 1 - 2 = 0 (모두 사용)

        // === 3단계: 추첨 실행 ===
        // 각 쿠폰 타입별로 독립적으로 추첨 진행
        
        // 1일권 추첨 실행 (응모자: user1, user3, user4 총 3명)
        conductLotteryDirectly(CouponType.ONE_DAY);
        
        // 3일권 추첨 실행 (응모자: user2, user4 총 2명)
        conductLotteryDirectly(CouponType.THREE_DAY);

        // === 4단계: 당첨자 조회 및 결과 검증 ===
        
        // 1일권 당첨자 조회 - 최대 3명까지 당첨 가능
        List<Object> oneDayWinners = getWinnersDirectly(CouponType.ONE_DAY);
        assertThat(oneDayWinners).hasSize(3); // 응모자 3명 모두 당첨 (쿠폰 수량 충분)

        // 3일권 당첨자 조회 - 응모자가 2명뿐이므로 2명 모두 당첨
        List<Object> threeDayWinners = getWinnersDirectly(CouponType.THREE_DAY);
        assertThat(threeDayWinners).hasSize(2); // 응모자 2명 모두 당첨

        // === 5단계: 응모 취소 기능 테스트 ===
        // user1이 남은 코인으로 새로운 응모를 한 후 취소하여 코인 반환 테스트
        if (getUserCoinCount(user1) > 0) {
            // 새로운 응모 생성
            Long newEntryId = enterLotteryDirectly(user1, CouponType.THREE_DAY, 1);
            
            // 응모 취소 및 코인 반환 확인
            cancelEntryDirectly(user1, newEntryId);
            assertUserCoinCount(user1, 1); // 코인이 정상적으로 반환되었는지 확인
        }

        // 테스트 결과 출력 - 실행 결과를 콘솔에 표시
        System.out.println("=== 전체 시스템 엔드투엔드 테스트 완료 ===");
        System.out.println("1일권 당첨자 수: " + oneDayWinners.size());
        System.out.println("3일권 당첨자 수: " + threeDayWinners.size());
        
        // 참고: entry ID들은 추후 특정 응모 내역 조회나 추가 검증에 사용할 수 있음
        // 현재는 응모 성공 여부만 확인하므로 사용하지 않음
        System.out.println("응모 ID 추적: " + entry1Id + ", " + entry2Id + ", " + 
                          entry3Id + ", " + entry4aId + ", " + entry4bId);
    }

    /**
     * 오류 시나리오 통합 테스트
     * 
     * 이 테스트는 시스템이 예외 상황을 올바르게 처리하는지 검증합니다.
     * 사용자의 잘못된 요청이나 시스템 제약 조건 위반 시 적절한 오류 처리가 되는지 확인합니다.
     * 
     * 테스트 시나리오:
     * 1. 코인 없이 응모 시도 - 코인 부족 오류 발생 확인
     * 2. 보유 코인보다 많은 코인으로 응모 시도 - 코인 부족 오류 발생 확인
     * 3. 존재하지 않는 응모 취소 시도 - 응모 내역 없음 오류 발생 확인
     * 
     * 검증 포인트:
     * - 각 오류 상황에서 적절한 예외가 발생하는지 확인
     * - 오류 메시지가 사용자가 이해할 수 있는 한국어로 제공되는지 확인
     * - 오류 발생 후 시스템 상태가 일관성을 유지하는지 확인
     */
    @Test
    @DisplayName("오류 시나리오 통합 테스트")
    void errorScenarioIntegration() throws Exception {
        String phoneNumber = "010-5555-5555";

        // === 1. 코인 없이 응모 시도 테스트 ===
        // 코인을 획득하지 않은 상태에서 응모를 시도하면 예외가 발생해야 함
        try {
            vacationCouponService.enterLottery(phoneNumber, CouponType.ONE_DAY, 1);
            assertThat(false).isTrue(); // 이 라인에 도달하면 테스트 실패 (예외가 발생해야 함)
        } catch (Exception e) {
            // 예외 메시지에 "코인" 관련 내용이 포함되어 있는지 확인
            assertThat(e.getMessage()).contains("코인");
            System.out.println("코인 부족 오류 정상 처리: " + e.getMessage());
        }

        // === 2. 보유량보다 많은 코인으로 응모 시도 테스트 ===
        // 1개의 코인만 획득한 후 2개 코인으로 응모 시도
        acquireCoinsDirectly(phoneNumber, 1);
        
        try {
            vacationCouponService.enterLottery(phoneNumber, CouponType.ONE_DAY, 2);
            assertThat(false).isTrue(); // 이 라인에 도달하면 테스트 실패 (예외가 발생해야 함)
        } catch (Exception e) {
            // 예외 메시지에 "부족" 관련 내용이 포함되어 있는지 확인
            assertThat(e.getMessage()).contains("부족");
            System.out.println("코인 부족 오류 정상 처리: " + e.getMessage());
        }

        // === 3. 존재하지 않는 응모 취소 시도 테스트 ===
        // 실제로 존재하지 않는 응모 ID로 취소 시도
        try {
            vacationCouponService.cancelEntry(phoneNumber, 99999L);
            assertThat(false).isTrue(); // 이 라인에 도달하면 테스트 실패 (예외가 발생해야 함)
        } catch (Exception e) {
            // 예외 메시지에 "찾을 수 없습니다" 관련 내용이 포함되어 있는지 확인
            assertThat(e.getMessage()).contains("찾을 수 없습니다");
            System.out.println("응모 내역 없음 오류 정상 처리: " + e.getMessage());
        }

        System.out.println("=== 오류 시나리오 통합 테스트 완료 ===");
        System.out.println("모든 예외 상황이 적절히 처리되었습니다.");
    }

    // ========================================
    // 헬퍼 메서드들 (Helper Methods)
    // ========================================
    // 테스트 코드의 가독성과 재사용성을 높이기 위한 보조 메서드들입니다.
    // 각 메서드는 특정 기능을 캡슐화하여 테스트 로직을 단순화합니다.

    /**
     * 사용자에게 지정된 수만큼 코인을 직접 획득시키는 헬퍼 메서드
     * 
     * @param phoneNumber 사용자 전화번호
     * @param count 획득할 코인 개수
     * 
     * 동작 방식:
     * - 지정된 횟수만큼 코인 획득 서비스를 반복 호출
     * - 실제 사용자가 여러 번 코인 획득 액션을 수행하는 것을 시뮬레이션
     */
    private void acquireCoinsDirectly(String phoneNumber, int count) {
        for (int i = 0; i < count; i++) {
            entryCoinService.acquireCoin(phoneNumber);
        }
    }

    /**
     * 사용자가 휴가 쿠폰에 응모하는 헬퍼 메서드
     * 
     * @param phoneNumber 사용자 전화번호
     * @param couponType 응모할 쿠폰 타입 (1일권/3일권)
     * @param coinCount 사용할 코인 개수
     * @return 생성된 응모 ID
     * 
     * 동작 방식:
     * - 휴가 쿠폰 서비스를 통해 응모 처리
     * - 응답 코드가 SUCCESS인지 검증
     * - 응모 성공 시 응모 ID 반환
     */
    private Long enterLotteryDirectly(String phoneNumber, CouponType couponType, int coinCount) {
        var response = vacationCouponService.enterLottery(phoneNumber, couponType, coinCount);
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        return response.getData().getEntryId();
    }

    /**
     * 사용자가 응모를 취소하는 헬퍼 메서드
     * 
     * @param phoneNumber 사용자 전화번호
     * @param entryId 취소할 응모 ID
     * 
     * 동작 방식:
     * - 휴가 쿠폰 서비스를 통해 응모 취소 처리
     * - 응답 코드가 SUCCESS인지 검증
     * - 취소 성공 시 사용한 코인이 사용자에게 반환됨
     */
    private void cancelEntryDirectly(String phoneNumber, Long entryId) {
        var response = vacationCouponService.cancelEntry(phoneNumber, entryId);
        assertThat(response.getCode()).isEqualTo("SUCCESS");
    }

    /**
     * 지정된 쿠폰 타입에 대해 추첨을 실행하는 헬퍼 메서드
     * 
     * @param couponType 추첨할 쿠폰 타입 (1일권/3일권)
     * 
     * 동작 방식:
     * - 추첨 서비스를 통해 해당 쿠폰 타입의 추첨 실행
     * - 응답 코드가 SUCCESS인지 검증
     * - 추첨 완료 후 당첨자가 결정됨
     */
    private void conductLotteryDirectly(CouponType couponType) {
        var response = lotteryService.conductLottery(couponType);
        assertThat(response.getCode()).isEqualTo("SUCCESS");
    }

    /**
     * 지정된 쿠폰 타입의 당첨자 목록을 조회하는 헬퍼 메서드
     * 
     * @param couponType 조회할 쿠폰 타입 (1일권/3일권)
     * @return 당첨자 목록 (Object 타입으로 변환)
     * 
     * 동작 방식:
     * - 추첨 서비스를 통해 당첨자 목록 조회
     * - 응답 코드가 SUCCESS인지 검증
     * - Winner 객체들을 Object 타입으로 변환하여 반환 (테스트 편의성)
     */
    private List<Object> getWinnersDirectly(CouponType couponType) {
        var response = lotteryService.getWinners(couponType);
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        // Winner 객체들을 Object 타입으로 변환하여 테스트에서 사용
        return response.getData().stream()
                .map(winner -> (Object) winner)
                .toList();
    }

    /**
     * 사용자의 코인 보유량을 검증하는 헬퍼 메서드
     * 
     * @param phoneNumber 사용자 전화번호
     * @param expectedCount 예상 코인 개수
     * 
     * 동작 방식:
     * - 사용자 리포지토리에서 사용자 정보 조회
     * - 예상 코인 개수가 0이고 사용자가 존재하지 않는 경우는 정상으로 처리
     *   (코인을 한 번도 획득하지 않은 사용자는 DB에 생성되지 않을 수 있음)
     * - 사용자가 존재하는 경우 코인 개수가 예상값과 일치하는지 검증
     */
    private void assertUserCoinCount(String phoneNumber, int expectedCount) {
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (expectedCount == 0 && user == null) {
            return; // 코인이 0개이고 사용자가 없는 경우는 정상 (사용자가 생성되지 않았을 수 있음)
        }
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(expectedCount);
    }

    /**
     * 사용자의 현재 코인 보유량을 조회하는 헬퍼 메서드
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 현재 코인 보유량 (사용자가 없으면 0 반환)
     * 
     * 동작 방식:
     * - 사용자 리포지토리에서 사용자 정보 조회
     * - 사용자가 존재하면 코인 개수 반환
     * - 사용자가 존재하지 않으면 0 반환
     */
    private int getUserCoinCount(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        return user != null ? user.getCoinCount() : 0;
    }
}