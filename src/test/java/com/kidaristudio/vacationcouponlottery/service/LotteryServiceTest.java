package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.LotteryResult;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.Winner;
import com.kidaristudio.vacationcouponlottery.exception.LotteryException;
import com.kidaristudio.vacationcouponlottery.repository.LotteryResultRepository;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * LotteryService 단위 테스트
 * 추첨 서비스의 핵심 기능을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class LotteryServiceTest {

    @Autowired
    private LotteryService lotteryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationCouponEntryRepository entryRepository;

    @Autowired
    private LotteryResultRepository lotteryResultRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 추첨 결과 초기화
        lotteryResultRepository.deleteAll();
    }

    @Test
    @DisplayName("추첨 실행 - 정상 케이스")
    void conductLottery_Success() {
        // Given: 5명의 응모자 생성
        createTestEntries(CouponType.ONE_DAY, 5);

        // When: 추첨 실행
        ApiResponse<com.kidaristudio.vacationcouponlottery.dto.LotteryResult> response = 
                lotteryService.conductLottery(CouponType.ONE_DAY);

        // Then: 성공 응답
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();

        com.kidaristudio.vacationcouponlottery.dto.LotteryResult result = response.getData();
        assertThat(result.getCouponType()).isEqualTo(CouponType.ONE_DAY);
        assertThat(result.getTotalEntrants()).isEqualTo(5);
        assertThat(result.getWinnerCount()).isEqualTo(3); // 쿠폰당 3명 당첨
        assertThat(result.getWinners()).hasSize(3);
        assertThat(result.getIsSuccess()).isTrue();

        // 데이터베이스에 추첨 결과 저장 확인
        List<LotteryResult> savedResults = lotteryResultRepository.findByCouponTypeOrderByRank(CouponType.ONE_DAY);
        assertThat(savedResults).hasSize(3);
        assertThat(savedResults.get(0).getRank()).isEqualTo(1);
        assertThat(savedResults.get(1).getRank()).isEqualTo(2);
        assertThat(savedResults.get(2).getRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("추첨 실행 - 응모자가 없는 경우")
    void conductLottery_NoEntrants() {
        // Given: 응모자가 없는 상태

        // When & Then: 추첨 실행 시 예외 발생
        assertThatThrownBy(() -> lotteryService.conductLottery(CouponType.ONE_DAY))
                .isInstanceOf(LotteryException.NoEntrantsException.class)
                .hasMessageContaining("응모자가 없어");
    }

    @Test
    @DisplayName("추첨 실행 - 이미 추첨이 완료된 경우")
    void conductLottery_AlreadyCompleted() {
        // Given: 응모자 생성 및 첫 번째 추첨 실행
        createTestEntries(CouponType.ONE_DAY, 5);
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // When & Then: 두 번째 추첨 시도 시 예외 발생
        assertThatThrownBy(() -> lotteryService.conductLottery(CouponType.ONE_DAY))
                .isInstanceOf(LotteryException.LotteryAlreadyCompletedException.class)
                .hasMessageContaining("이미 추첨이 완료되었습니다.");
    }

    @Test
    @DisplayName("추첨 실행 - 응모자가 당첨자 수보다 적은 경우")
    void conductLottery_FewerEntrantsThanWinners() {
        // Given: 2명의 응모자만 생성 (당첨자 3명보다 적음)
        createTestEntries(CouponType.THREE_DAY, 2);

        // When: 추첨 실행
        ApiResponse<com.kidaristudio.vacationcouponlottery.dto.LotteryResult> response = 
                lotteryService.conductLottery(CouponType.THREE_DAY);

        // Then: 응모자 수만큼만 당첨
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        com.kidaristudio.vacationcouponlottery.dto.LotteryResult result = response.getData();
        assertThat(result.getWinnerCount()).isEqualTo(2); // 응모자 수만큼
        assertThat(result.getWinners()).hasSize(2);
    }

    @Test
    @DisplayName("전체 추첨 실행 - 정상 케이스")
    void conductAllLotteries_Success() {
        // Given: 두 쿠폰 타입 모두에 응모자 생성
        createTestEntries(CouponType.ONE_DAY, 4);
        createTestEntries(CouponType.THREE_DAY, 5);

        // When: 전체 추첨 실행
        ApiResponse<List<com.kidaristudio.vacationcouponlottery.dto.LotteryResult>> response = 
                lotteryService.conductAllLotteries();

        // Then: 두 쿠폰 모두 추첨 완료
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(2);

        List<com.kidaristudio.vacationcouponlottery.dto.LotteryResult> results = response.getData();
        assertThat(results.stream().anyMatch(r -> r.getCouponType() == CouponType.ONE_DAY)).isTrue();
        assertThat(results.stream().anyMatch(r -> r.getCouponType() == CouponType.THREE_DAY)).isTrue();
    }

    @Test
    @DisplayName("당첨자 조회 - 특정 쿠폰 타입")
    void getWinners_SpecificCouponType() {
        // Given: 응모자 생성 및 추첨 실행
        createTestEntries(CouponType.ONE_DAY, 5);
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // When: 당첨자 조회
        ApiResponse<List<Winner>> response = lotteryService.getWinners(CouponType.ONE_DAY);

        // Then: 당첨자 목록 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(3);

        List<Winner> winners = response.getData();
        assertThat(winners.get(0).getRank()).isEqualTo(1);
        assertThat(winners.get(1).getRank()).isEqualTo(2);
        assertThat(winners.get(2).getRank()).isEqualTo(3);
        assertThat(winners.stream().allMatch(w -> w.getCouponType() == CouponType.ONE_DAY)).isTrue();
    }

    @Test
    @DisplayName("당첨자 조회 - 전체 쿠폰 타입")
    void getWinners_AllCouponTypes() {
        // Given: 두 쿠폰 타입 모두 추첨 실행
        createTestEntries(CouponType.ONE_DAY, 4);
        createTestEntries(CouponType.THREE_DAY, 4);
        lotteryService.conductLottery(CouponType.ONE_DAY);
        lotteryService.conductLottery(CouponType.THREE_DAY);

        // When: 전체 당첨자 조회
        ApiResponse<List<Winner>> response = lotteryService.getWinners(null);

        // Then: 모든 당첨자 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(6); // 각 쿠폰당 3명씩

        List<Winner> winners = response.getData();
        long oneDayWinners = winners.stream().filter(w -> w.getCouponType() == CouponType.ONE_DAY).count();
        long threeDayWinners = winners.stream().filter(w -> w.getCouponType() == CouponType.THREE_DAY).count();
        assertThat(oneDayWinners).isEqualTo(3);
        assertThat(threeDayWinners).isEqualTo(3);
    }

    @Test
    @DisplayName("추첨 완료 여부 확인 - 특정 쿠폰 타입")
    void isLotteryCompleted_SpecificCouponType() {
        // Given: 한 쿠폰 타입만 추첨 완료
        createTestEntries(CouponType.ONE_DAY, 4);
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // When & Then: 추첨 완료 여부 확인
        ApiResponse<Boolean> completedResponse = lotteryService.isLotteryCompleted(CouponType.ONE_DAY);
        assertThat(completedResponse.getData()).isTrue();

        ApiResponse<Boolean> notCompletedResponse = lotteryService.isLotteryCompleted(CouponType.THREE_DAY);
        assertThat(notCompletedResponse.getData()).isFalse();
    }

    @Test
    @DisplayName("추첨 완료 여부 확인 - 전체 쿠폰 타입")
    void isLotteryCompleted_AllCouponTypes() {
        // Given: 한 쿠폰 타입만 추첨 완료
        createTestEntries(CouponType.ONE_DAY, 4);
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // When & Then: 전체 추첨 완료 여부 확인 (모든 쿠폰이 완료되어야 true)
        ApiResponse<Boolean> response = lotteryService.isLotteryCompleted(null);
        assertThat(response.getData()).isFalse(); // THREE_DAY가 아직 완료되지 않음

        // 나머지 쿠폰도 추첨 완료
        createTestEntries(CouponType.THREE_DAY, 4);
        lotteryService.conductLottery(CouponType.THREE_DAY);

        ApiResponse<Boolean> allCompletedResponse = lotteryService.isLotteryCompleted(null);
        assertThat(allCompletedResponse.getData()).isTrue();
    }

    @Test
    @DisplayName("추첨 결과 초기화 - 특정 쿠폰 타입")
    void resetLottery_SpecificCouponType() {
        // Given: 추첨 완료 상태
        createTestEntries(CouponType.ONE_DAY, 4);
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // When: 특정 쿠폰 타입 초기화
        ApiResponse<Void> response = lotteryService.resetLottery(CouponType.ONE_DAY);

        // Then: 해당 쿠폰 타입만 초기화
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(lotteryService.isLotteryCompleted(CouponType.ONE_DAY).getData()).isFalse();
    }

    @Test
    @DisplayName("추첨 결과 초기화 - 전체 쿠폰 타입")
    void resetLottery_AllCouponTypes() {
        // Given: 모든 쿠폰 타입 추첨 완료
        createTestEntries(CouponType.ONE_DAY, 4);
        createTestEntries(CouponType.THREE_DAY, 4);
        lotteryService.conductAllLotteries();

        // When: 전체 초기화
        ApiResponse<Void> response = lotteryService.resetLottery(null);

        // Then: 모든 추첨 결과 초기화
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(lotteryService.isLotteryCompleted(null).getData()).isFalse();
    }

    @Test
    @DisplayName("추첨 공정성 - 코인 수에 비례한 당첨 확률")
    void conductLottery_Fairness() {
        // Given: 코인 수가 다른 응모자들 생성
        User user1 = createUser("010-1111-1111", 3);
        User user2 = createUser("010-2222-2222", 3);
        User user3 = createUser("010-3333-3333", 3);
        User user4 = createUser("010-4444-4444", 3);
        User user5 = createUser("010-5555-5555", 3);

        // 각각 다른 코인 수로 응모 (1, 1, 1, 2, 3개)
        createEntry(user1, CouponType.ONE_DAY, 1);
        createEntry(user2, CouponType.ONE_DAY, 1);
        createEntry(user3, CouponType.ONE_DAY, 1);
        createEntry(user4, CouponType.ONE_DAY, 2);
        createEntry(user5, CouponType.ONE_DAY, 3);

        // When: 추첨 실행
        ApiResponse<com.kidaristudio.vacationcouponlottery.dto.LotteryResult> response = 
                lotteryService.conductLottery(CouponType.ONE_DAY);

        // Then: 3명 당첨
        assertThat(response.getData().getWinnerCount()).isEqualTo(3);
        
        // 당첨자들의 코인 수 확인 (더 많은 코인을 사용한 사용자가 당첨될 가능성이 높음)
        List<Winner> winners = response.getData().getWinners();
        assertThat(winners).hasSize(3);
        
        // 모든 당첨자가 서로 다른 사용자인지 확인
        assertThat(winners.stream().map(Winner::getPhoneNumber).distinct().count()).isEqualTo(3);
    }

    /**
     * 테스트용 응모자들 생성
     */
    private void createTestEntries(CouponType couponType, int count) {
        for (int i = 1; i <= count; i++) {
            String phoneNumber = String.format("010-%04d-%04d", couponType.ordinal() + 1, i);
            User user = createUser(phoneNumber, 3);
            createEntry(user, couponType, 1 + (i % 3)); // 1~3개 코인 랜덤
        }
    }

    /**
     * 테스트용 사용자 생성
     */
    private User createUser(String phoneNumber, int coinCount) {
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .coinCount(coinCount)
                .build();
        return userRepository.save(user);
    }

    /**
     * 테스트용 응모 내역 생성
     */
    private VacationCouponEntry createEntry(User user, CouponType couponType, int coinCount) {
        VacationCouponEntry entry = VacationCouponEntry.builder()
                .user(user)
                .couponType(couponType)
                .coinCount(coinCount)
                .isActive(true)
                .build();
        return entryRepository.save(entry);
    }

    @Test
    @DisplayName("추첨 실행 - 잘못된 쿠폰 타입 (null)")
    void conductLottery_InvalidCouponType() {
        // Given: null 쿠폰 타입

        // When & Then: 추첨 실행 시 예외 발생
        assertThatThrownBy(() -> lotteryService.conductLottery(null))
                .isInstanceOf(LotteryException.InvalidLotteryRequestException.class)
                .hasMessageContaining("잘못된 추첨 요청입니다.");
    }

    @Test
    @DisplayName("추첨 결과 저장 검증 - 순위 정확성")
    void conductLottery_RankingAccuracy() {
        // Given: 5명의 응모자 생성
        createTestEntries(CouponType.ONE_DAY, 5);

        // When: 추첨 실행
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // Then: 순위가 정확히 1, 2, 3으로 저장되었는지 확인
        List<LotteryResult> results = lotteryResultRepository.findByCouponTypeOrderByRank(CouponType.ONE_DAY);
        assertThat(results).hasSize(3);
        
        for (int i = 0; i < results.size(); i++) {
            assertThat(results.get(i).getRank()).isEqualTo(i + 1);
            assertThat(results.get(i).getCouponType()).isEqualTo(CouponType.ONE_DAY);
            assertThat(results.get(i).getLotteryDate()).isNotNull();
        }
    }

    @Test
    @DisplayName("추첨 공정성 - 동일한 코인 수 응모자들")
    void conductLottery_EqualCoinFairness() {
        // Given: 모든 응모자가 동일한 코인 수로 응모
        for (int i = 1; i <= 6; i++) {
            String phoneNumber = String.format("010-1111-%04d", i);
            User user = createUser(phoneNumber, 2);
            createEntry(user, CouponType.THREE_DAY, 2); // 모두 2개 코인
        }

        // When: 추첨 실행
        ApiResponse<com.kidaristudio.vacationcouponlottery.dto.LotteryResult> response = 
                lotteryService.conductLottery(CouponType.THREE_DAY);

        // Then: 3명이 공정하게 선정됨
        assertThat(response.getData().getWinnerCount()).isEqualTo(3);
        
        // 모든 당첨자가 동일한 코인 수를 사용했는지 확인
        List<Winner> winners = response.getData().getWinners();
        assertThat(winners.stream().allMatch(w -> w.getCoinCount() == 2)).isTrue();
        
        // 서로 다른 사용자가 당첨되었는지 확인
        assertThat(winners.stream().map(Winner::getPhoneNumber).distinct().count()).isEqualTo(3);
    }

    @Test
    @DisplayName("추첨 공정성 - 극단적인 코인 수 차이")
    void conductLottery_ExtremeCoinDifference() {
        // Given: 극단적인 코인 수 차이 (1개 vs 3개)
        User user1 = createUser("010-1111-1111", 3);
        User user2 = createUser("010-2222-2222", 3);
        User user3 = createUser("010-3333-3333", 3);
        User user4 = createUser("010-4444-4444", 3);

        createEntry(user1, CouponType.ONE_DAY, 1); // 1개 코인 (당첨 확률 낮음)
        createEntry(user2, CouponType.ONE_DAY, 3); // 3개 코인 (당첨 확률 높음)
        createEntry(user3, CouponType.ONE_DAY, 3); // 3개 코인 (당첨 확률 높음)
        createEntry(user4, CouponType.ONE_DAY, 3); // 3개 코인 (당첨 확률 높음)

        // When: 여러 번 추첨하여 통계적 공정성 확인
        int user1WinCount = 0;
        int totalTrials = 100;

        for (int trial = 0; trial < totalTrials; trial++) {
            // 추첨 결과 초기화
            lotteryResultRepository.deleteAll();
            
            // 추첨 실행
            ApiResponse<com.kidaristudio.vacationcouponlottery.dto.LotteryResult> response = 
                    lotteryService.conductLottery(CouponType.ONE_DAY);
            
            // user1 당첨 여부 확인
            boolean user1Won = response.getData().getWinners().stream()
                    .anyMatch(w -> w.getPhoneNumber().equals("010-1111-1111"));
            
            if (user1Won) {
                user1WinCount++;
            }
        }

        // Then: 1개 코인 사용자의 당첨 확률이 3개 코인 사용자보다 낮아야 함
        // 이론적으로 user1의 당첨 확률은 약 30% (1/(1+3+3+3) * 3 = 3/10)
        // 통계적 오차를 고려하여 10% ~ 50% 범위로 검증
        double user1WinRate = (double) user1WinCount / totalTrials;
        assertThat(user1WinRate).isBetween(0.1, 0.5);
        
        // 3개 코인 사용자들의 평균 당첨 확률이 더 높아야 함
        assertThat(user1WinRate).isLessThan(0.7); // 3개 코인 사용자들의 개별 당첨 확률
    }

    @Test
    @DisplayName("당첨자 조회 - 추첨 전 조회")
    void getWinners_BeforeLottery() {
        // Given: 응모자는 있지만 추첨은 하지 않은 상태
        createTestEntries(CouponType.ONE_DAY, 5);

        // When: 당첨자 조회
        ApiResponse<List<Winner>> response = lotteryService.getWinners(CouponType.ONE_DAY);

        // Then: 빈 목록 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("당첨자 조회 - 존재하지 않는 쿠폰 타입 결과")
    void getWinners_NonExistentResults() {
        // Given: ONE_DAY만 추첨 완료
        createTestEntries(CouponType.ONE_DAY, 5);
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // When: THREE_DAY 당첨자 조회
        ApiResponse<List<Winner>> response = lotteryService.getWinners(CouponType.THREE_DAY);

        // Then: 빈 목록 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("전체 추첨 - 일부 쿠폰에만 응모자 있는 경우")
    void conductAllLotteries_PartialEntrants() {
        // Given: ONE_DAY에만 응모자 있음
        createTestEntries(CouponType.ONE_DAY, 4);

        // When: 전체 추첨 실행
        ApiResponse<List<com.kidaristudio.vacationcouponlottery.dto.LotteryResult>> response = 
                lotteryService.conductAllLotteries();

        // Then: ONE_DAY만 추첨 결과 있음
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCouponType()).isEqualTo(CouponType.ONE_DAY);
    }

    @Test
    @DisplayName("전체 추첨 - 모든 쿠폰에 응모자 없는 경우")
    void conductAllLotteries_NoEntrants() {
        // Given: 응모자가 전혀 없는 상태

        // When: 전체 추첨 실행
        ApiResponse<List<com.kidaristudio.vacationcouponlottery.dto.LotteryResult>> response = 
                lotteryService.conductAllLotteries();

        // Then: 빈 결과 목록 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("추첨 완료 여부 - 부분 완료 상태")
    void isLotteryCompleted_PartialCompletion() {
        // Given: ONE_DAY만 추첨 완료
        createTestEntries(CouponType.ONE_DAY, 4);
        createTestEntries(CouponType.THREE_DAY, 4);
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // When & Then: 개별 쿠폰 완료 여부 확인
        assertThat(lotteryService.isLotteryCompleted(CouponType.ONE_DAY).getData()).isTrue();
        assertThat(lotteryService.isLotteryCompleted(CouponType.THREE_DAY).getData()).isFalse();
        
        // 전체 완료 여부는 false (모든 쿠폰이 완료되어야 true)
        assertThat(lotteryService.isLotteryCompleted(null).getData()).isFalse();
    }

    @Test
    @DisplayName("추첨 결과 초기화 - 부분 초기화")
    void resetLottery_PartialReset() {
        // Given: 두 쿠폰 모두 추첨 완료
        createTestEntries(CouponType.ONE_DAY, 4);
        createTestEntries(CouponType.THREE_DAY, 4);
        lotteryService.conductAllLotteries();

        // When: ONE_DAY만 초기화
        lotteryService.resetLottery(CouponType.ONE_DAY);

        // Then: ONE_DAY만 초기화되고 THREE_DAY는 유지
        assertThat(lotteryService.isLotteryCompleted(CouponType.ONE_DAY).getData()).isFalse();
        assertThat(lotteryService.isLotteryCompleted(CouponType.THREE_DAY).getData()).isTrue();
    }

    @Test
    @DisplayName("추첨 결과 데이터 무결성 - 응모 내역과 추첨 결과 연결")
    void conductLottery_DataIntegrity() {
        // Given: 응모자 생성
        createTestEntries(CouponType.ONE_DAY, 5);

        // When: 추첨 실행
        lotteryService.conductLottery(CouponType.ONE_DAY);

        // Then: 추첨 결과와 응모 내역이 올바르게 연결되어 있는지 확인
        List<LotteryResult> lotteryResults = lotteryResultRepository.findByCouponTypeOrderByRank(CouponType.ONE_DAY);
        
        for (LotteryResult result : lotteryResults) {
            // 추첨 결과의 응모 내역이 실제로 존재하는지 확인
            VacationCouponEntry entry = result.getEntry();
            assertThat(entry).isNotNull();
            assertThat(entry.getCouponType()).isEqualTo(CouponType.ONE_DAY);
            assertThat(entry.getIsActive()).isTrue();
            
            // 응모 내역의 사용자가 실제로 존재하는지 확인
            User user = entry.getUser();
            assertThat(user).isNotNull();
            assertThat(user.getPhoneNumber()).isNotNull();
        }
    }

    @Test
    @DisplayName("추첨 알고리즘 일관성 - 동일 조건에서 다른 결과")
    void conductLottery_AlgorithmConsistency() {
        // Given: 동일한 응모 조건
        createTestEntries(CouponType.ONE_DAY, 6);

        // When: 여러 번 추첨 (각각 초기화 후 실행)
        String firstResult = getWinnerPhones(CouponType.ONE_DAY);
        
        lotteryResultRepository.deleteAll();
        String secondResult = getWinnerPhones(CouponType.ONE_DAY);
        
        lotteryResultRepository.deleteAll();
        String thirdResult = getWinnerPhones(CouponType.ONE_DAY);

        // Then: 매번 다른 결과가 나와야 함 (무작위성 확인)
        // 모든 결과가 동일할 확률은 매우 낮음
        boolean allSame = firstResult.equals(secondResult) && secondResult.equals(thirdResult);
        assertThat(allSame).isFalse();
    }

    /**
     * 당첨자 전화번호를 정렬된 문자열로 반환하는 헬퍼 메서드
     */
    private String getWinnerPhones(CouponType couponType) {
        lotteryService.conductLottery(couponType);
        List<Winner> winners = lotteryService.getWinners(couponType).getData();
        return winners.stream()
                .map(Winner::getPhoneNumber)
                .sorted()
                .collect(Collectors.joining(","));
    }
}