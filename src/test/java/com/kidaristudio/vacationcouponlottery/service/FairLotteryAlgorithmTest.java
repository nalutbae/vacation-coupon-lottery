package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * FairLotteryAlgorithm 단위 테스트
 * 공정한 추첨 알고리즘의 정확성과 공정성을 검증합니다.
 */
class FairLotteryAlgorithmTest {

    private FairLotteryAlgorithm lotteryAlgorithm;

    @BeforeEach
    void setUp() {
        lotteryAlgorithm = new FairLotteryAlgorithm();
    }

    @Test
    @DisplayName("공정한 추첨 - 정상 케이스")
    void conductFairLottery_Success() {
        // Given: 3명의 응모자, 각각 1, 2, 3개 코인
        List<VacationCouponEntry> entries = createTestEntries();
        int winnerCount = 2;

        // When: 추첨 실행
        List<VacationCouponEntry> winners = lotteryAlgorithm.conductFairLottery(entries, winnerCount);

        // Then: 정확히 2명 당첨
        assertThat(winners).hasSize(2);

        // 중복 당첨자 없음 확인
        Set<Long> winnerUserIds = winners.stream()
                .map(entry -> entry.getUser().getId())
                .collect(Collectors.toSet());
        assertThat(winnerUserIds).hasSize(2);

        // 모든 당첨자가 원래 응모자 목록에 포함되어 있음
        Set<Long> entrantUserIds = entries.stream()
                .map(entry -> entry.getUser().getId())
                .collect(Collectors.toSet());
        assertThat(winnerUserIds).isSubsetOf(entrantUserIds);
    }

    @Test
    @DisplayName("공정한 추첨 - 응모자가 없는 경우")
    void conductFairLottery_NoEntrants() {
        // Given: 빈 응모자 목록
        List<VacationCouponEntry> entries = Collections.emptyList();
        int winnerCount = 3;

        // When: 추첨 실행
        List<VacationCouponEntry> winners = lotteryAlgorithm.conductFairLottery(entries, winnerCount);

        // Then: 빈 당첨자 목록 반환
        assertThat(winners).isEmpty();
    }

    @Test
    @DisplayName("공정한 추첨 - 당첨자 수가 0인 경우")
    void conductFairLottery_ZeroWinners() {
        // Given: 응모자 목록
        List<VacationCouponEntry> entries = createTestEntries();
        int winnerCount = 0;

        // When: 추첨 실행
        List<VacationCouponEntry> winners = lotteryAlgorithm.conductFairLottery(entries, winnerCount);

        // Then: 빈 당첨자 목록 반환
        assertThat(winners).isEmpty();
    }

    @Test
    @DisplayName("공정한 추첨 - 당첨자 수가 응모자 수보다 많은 경우")
    void conductFairLottery_MoreWinnersThanEntrants() {
        // Given: 3명의 응모자
        List<VacationCouponEntry> entries = createTestEntries();
        int winnerCount = 5; // 응모자보다 많은 당첨자 수

        // When: 추첨 실행
        List<VacationCouponEntry> winners = lotteryAlgorithm.conductFairLottery(entries, winnerCount);

        // Then: 응모자 수만큼만 당첨 (3명)
        assertThat(winners).hasSize(3);

        // 모든 응모자가 당첨
        Set<Long> winnerUserIds = winners.stream()
                .map(entry -> entry.getUser().getId())
                .collect(Collectors.toSet());
        Set<Long> entrantUserIds = entries.stream()
                .map(entry -> entry.getUser().getId())
                .collect(Collectors.toSet());
        assertThat(winnerUserIds).isEqualTo(entrantUserIds);
    }

    @Test
    @DisplayName("공정한 추첨 - 단일 응모자")
    void conductFairLottery_SingleEntrant() {
        // Given: 1명의 응모자
        List<VacationCouponEntry> entries = List.of(createTestEntry("010-1111-1111", 2));
        int winnerCount = 1;

        // When: 추첨 실행
        List<VacationCouponEntry> winners = lotteryAlgorithm.conductFairLottery(entries, winnerCount);

        // Then: 해당 응모자가 당첨
        assertThat(winners).hasSize(1);
        assertThat(winners.get(0).getUser().getPhoneNumber()).isEqualTo("010-1111-1111");
    }

    @Test
    @DisplayName("추첨 공정성 - 코인 수에 비례한 당첨 확률")
    void conductFairLottery_Fairness() {
        // Given: 코인 수가 다른 두 응모자 (1개 vs 9개)
        List<VacationCouponEntry> entries = List.of(
                createTestEntry("010-1111-1111", 1), // 10% 확률
                createTestEntry("010-2222-2222", 9)  // 90% 확률
        );
        int winnerCount = 1;
        int iterations = 1000;

        // When: 여러 번 추첨 실행
        Map<String, Integer> winCounts = new HashMap<>();
        winCounts.put("010-1111-1111", 0);
        winCounts.put("010-2222-2222", 0);

        for (int i = 0; i < iterations; i++) {
            List<VacationCouponEntry> winners = lotteryAlgorithm.conductFairLottery(entries, winnerCount);
            if (!winners.isEmpty()) {
                String winnerPhone = winners.get(0).getUser().getPhoneNumber();
                winCounts.put(winnerPhone, winCounts.get(winnerPhone) + 1);
            }
        }

        // Then: 9개 코인 보유자가 더 많이 당첨되어야 함 (통계적 검증)
        int wins1Coin = winCounts.get("010-1111-1111");
        int wins9Coins = winCounts.get("010-2222-2222");

        // 9개 코인 보유자가 1개 코인 보유자보다 더 많이 당첨되어야 함
        assertThat(wins9Coins).isGreaterThan(wins1Coin);

        // 대략적인 비율 확인 (오차 허용)
        double ratio = (double) wins9Coins / wins1Coin;
        assertThat(ratio).isBetween(5.0, 15.0); // 이론적으로는 9:1이지만 통계적 오차 고려
    }

    @Test
    @DisplayName("추첨 통계 계산 - 정상 케이스")
    void calculateStatistics_Success() {
        // Given: 3명의 응모자
        List<VacationCouponEntry> entries = createTestEntries(); // 1, 2, 3개 코인

        // When: 통계 계산
        FairLotteryAlgorithm.LotteryStatistics stats = lotteryAlgorithm.calculateStatistics(entries);

        // Then: 정확한 통계 정보
        assertThat(stats.getTotalEntrants()).isEqualTo(3);
        assertThat(stats.getTotalCoins()).isEqualTo(6); // 1 + 2 + 3
        assertThat(stats.getAverageCoins()).isEqualTo(2.0); // 6 / 3
        assertThat(stats.getMaxCoins()).isEqualTo(3);
        assertThat(stats.getMinCoins()).isEqualTo(1);
    }

    @Test
    @DisplayName("추첨 통계 계산 - 빈 목록")
    void calculateStatistics_EmptyList() {
        // Given: 빈 응모자 목록
        List<VacationCouponEntry> entries = Collections.emptyList();

        // When: 통계 계산
        FairLotteryAlgorithm.LotteryStatistics stats = lotteryAlgorithm.calculateStatistics(entries);

        // Then: 모든 값이 0
        assertThat(stats.getTotalEntrants()).isEqualTo(0);
        assertThat(stats.getTotalCoins()).isEqualTo(0);
        assertThat(stats.getAverageCoins()).isEqualTo(0.0);
        assertThat(stats.getMaxCoins()).isEqualTo(0);
        assertThat(stats.getMinCoins()).isEqualTo(0);
    }

    @Test
    @DisplayName("추첨 결과 일관성 - 동일한 입력에 대해 다른 결과")
    void conductFairLottery_Randomness() {
        // Given: 동일한 응모자 목록
        List<VacationCouponEntry> entries = createTestEntries();
        int winnerCount = 2;

        // When: 여러 번 추첨 실행
        Set<String> differentResults = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            List<VacationCouponEntry> winners = lotteryAlgorithm.conductFairLottery(entries, winnerCount);
            String result = winners.stream()
                    .map(entry -> entry.getUser().getPhoneNumber())
                    .sorted()
                    .collect(Collectors.joining(","));
            differentResults.add(result);
        }

        // Then: 여러 가지 다른 결과가 나와야 함 (무작위성 확인)
        assertThat(differentResults.size()).isGreaterThan(1);
    }

    /**
     * 테스트용 응모 내역 목록 생성
     * 3명의 사용자가 각각 1, 2, 3개의 코인으로 응모
     */
    private List<VacationCouponEntry> createTestEntries() {
        return List.of(
                createTestEntry("010-1111-1111", 1),
                createTestEntry("010-2222-2222", 2),
                createTestEntry("010-3333-3333", 3)
        );
    }

    /**
     * 테스트용 응모 내역 생성
     */
    private VacationCouponEntry createTestEntry(String phoneNumber, int coinCount) {
        User user = User.builder()
                .id((long) phoneNumber.hashCode()) // 고유 ID 생성
                .phoneNumber(phoneNumber)
                .coinCount(coinCount)
                .build();

        return VacationCouponEntry.builder()
                .id((long) (phoneNumber + coinCount).hashCode()) // 고유 ID 생성
                .user(user)
                .couponType(CouponType.ONE_DAY)
                .coinCount(coinCount)
                .isActive(true)
                .build();
    }
}