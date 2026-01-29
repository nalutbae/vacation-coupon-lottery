package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;

/**
 * 공정한 추첨 알고리즘 구현체
 * Fisher-Yates 셔플 알고리즘을 사용하여 응모 코인 수에 비례한 당첨 확률을 보장합니다.
 */
@Slf4j
@Component
public class FairLotteryAlgorithm {

    private final SecureRandom secureRandom;

    public FairLotteryAlgorithm() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * 공정한 추첨을 수행합니다.
     * 각 응모자의 응모 코인 수만큼 추첨 기회를 부여하여 공정성을 보장합니다.
     *
     * @param entries 응모 내역 목록
     * @param winnerCount 당첨자 수
     * @return 당첨자 목록 (순위 순서)
     */
    public List<VacationCouponEntry> conductFairLottery(List<VacationCouponEntry> entries, int winnerCount) {
        log.info("공정한 추첨 시작: 총 응모자 {}명, 당첨자 {}명", entries.size(), winnerCount);

        if (entries.isEmpty()) {
            log.warn("응모자가 없어 추첨을 진행할 수 없습니다.");
            return Collections.emptyList();
        }

        if (winnerCount <= 0) {
            log.warn("당첨자 수가 0 이하입니다: {}", winnerCount);
            return Collections.emptyList();
        }

        // 1. 응모 코인 수에 비례한 추첨 풀 생성
        List<VacationCouponEntry> lotteryPool = createLotteryPool(entries);
        log.debug("추첨 풀 생성 완료: 총 {}개 추첨 기회", lotteryPool.size());

        // 2. Fisher-Yates 셔플로 무작위 순서 생성
        shuffleLotteryPool(lotteryPool);
        log.debug("추첨 풀 셔플 완료");

        // 3. 중복 제거하여 당첨자 선정
        List<VacationCouponEntry> winners = selectUniqueWinners(lotteryPool, winnerCount);
        log.info("추첨 완료: {}명 당첨", winners.size());

        return winners;
    }

    /**
     * 응모 코인 수에 비례한 추첨 풀을 생성합니다.
     * 각 응모자의 응모 코인 수만큼 추첨 기회를 부여합니다.
     *
     * @param entries 응모 내역 목록
     * @return 추첨 풀 (응모 코인 수만큼 중복 포함)
     */
    private List<VacationCouponEntry> createLotteryPool(List<VacationCouponEntry> entries) {
        List<VacationCouponEntry> pool = new ArrayList<>();
        
        for (VacationCouponEntry entry : entries) {
            // 응모 코인 수만큼 추첨 기회 부여
            int coinCount = entry.getCoinCount();
            for (int i = 0; i < coinCount; i++) {
                pool.add(entry);
            }
            log.trace("사용자 {}에게 {}개 추첨 기회 부여", 
                    entry.getUser().getPhoneNumber(), coinCount);
        }

        return pool;
    }

    /**
     * Fisher-Yates 셔플 알고리즘을 사용하여 추첨 풀을 무작위로 섞습니다.
     * 암호학적으로 안전한 SecureRandom을 사용합니다.
     *
     * @param lotteryPool 추첨 풀
     */
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

    /**
     * 셔플된 추첨 풀에서 중복 없이 당첨자를 선정합니다.
     * 동일한 사용자는 한 번만 당첨될 수 있습니다.
     *
     * @param shuffledPool 셔플된 추첨 풀
     * @param winnerCount 당첨자 수
     * @return 당첨자 목록 (순위 순서)
     */
    private List<VacationCouponEntry> selectUniqueWinners(List<VacationCouponEntry> shuffledPool, int winnerCount) {
        Set<Long> selectedUserIds = new HashSet<>();
        List<VacationCouponEntry> winners = new ArrayList<>();

        for (VacationCouponEntry entry : shuffledPool) {
            if (winners.size() >= winnerCount) {
                break;
            }

            Long userId = entry.getUser().getId();
            if (!selectedUserIds.contains(userId)) {
                selectedUserIds.add(userId);
                winners.add(entry);
                log.debug("{}순위 당첨자 선정: 사용자 {}, 응모 코인 {}개", 
                        winners.size(), entry.getUser().getPhoneNumber(), entry.getCoinCount());
            }
        }

        return winners;
    }

    /**
     * 추첨 통계 정보를 계산합니다.
     *
     * @param entries 응모 내역 목록
     * @return 추첨 통계 정보
     */
    public LotteryStatistics calculateStatistics(List<VacationCouponEntry> entries) {
        if (entries.isEmpty()) {
            return LotteryStatistics.empty();
        }

        int totalEntrants = entries.size();
        int totalCoins = entries.stream().mapToInt(VacationCouponEntry::getCoinCount).sum();
        double averageCoins = (double) totalCoins / totalEntrants;
        int maxCoins = entries.stream().mapToInt(VacationCouponEntry::getCoinCount).max().orElse(0);
        int minCoins = entries.stream().mapToInt(VacationCouponEntry::getCoinCount).min().orElse(0);

        return LotteryStatistics.builder()
                .totalEntrants(totalEntrants)
                .totalCoins(totalCoins)
                .averageCoins(averageCoins)
                .maxCoins(maxCoins)
                .minCoins(minCoins)
                .build();
    }

    /**
     * 추첨 통계 정보를 담는 내부 클래스
     */
    public static class LotteryStatistics {
        private final int totalEntrants;
        private final int totalCoins;
        private final double averageCoins;
        private final int maxCoins;
        private final int minCoins;

        private LotteryStatistics(int totalEntrants, int totalCoins, double averageCoins, 
                                int maxCoins, int minCoins) {
            this.totalEntrants = totalEntrants;
            this.totalCoins = totalCoins;
            this.averageCoins = averageCoins;
            this.maxCoins = maxCoins;
            this.minCoins = minCoins;
        }

        public static LotteryStatistics empty() {
            return new LotteryStatistics(0, 0, 0.0, 0, 0);
        }

        public static LotteryStatisticsBuilder builder() {
            return new LotteryStatisticsBuilder();
        }

        // Getters
        public int getTotalEntrants() { return totalEntrants; }
        public int getTotalCoins() { return totalCoins; }
        public double getAverageCoins() { return averageCoins; }
        public int getMaxCoins() { return maxCoins; }
        public int getMinCoins() { return minCoins; }

        public static class LotteryStatisticsBuilder {
            private int totalEntrants;
            private int totalCoins;
            private double averageCoins;
            private int maxCoins;
            private int minCoins;

            public LotteryStatisticsBuilder totalEntrants(int totalEntrants) {
                this.totalEntrants = totalEntrants;
                return this;
            }

            public LotteryStatisticsBuilder totalCoins(int totalCoins) {
                this.totalCoins = totalCoins;
                return this;
            }

            public LotteryStatisticsBuilder averageCoins(double averageCoins) {
                this.averageCoins = averageCoins;
                return this;
            }

            public LotteryStatisticsBuilder maxCoins(int maxCoins) {
                this.maxCoins = maxCoins;
                return this;
            }

            public LotteryStatisticsBuilder minCoins(int minCoins) {
                this.minCoins = minCoins;
                return this;
            }

            public LotteryStatistics build() {
                return new LotteryStatistics(totalEntrants, totalCoins, averageCoins, maxCoins, minCoins);
            }
        }
    }
}