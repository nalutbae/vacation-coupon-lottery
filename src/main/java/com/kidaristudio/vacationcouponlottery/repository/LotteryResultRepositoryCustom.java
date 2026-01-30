package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.LotteryResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 추첨 결과 Repository 커스텀 인터페이스
 * QueryDSL을 활용한 복잡한 추첨 결과 조회 메서드를 정의합니다.
 */
public interface LotteryResultRepositoryCustom {

    /**
     * 쿠폰 타입별 당첨자 수 집계
     */
    Map<CouponType, Long> countWinnersByCouponType();

    /**
     * 순위별 당첨자 수 집계
     */
    Map<Integer, Long> countWinnersByRank();

    /**
     * 특정 기간 내 추첨 결과 조회
     */
    List<LotteryResult> findResultsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 최근 추첨 결과 조회
     */
    List<LotteryResult> findRecentResults(int limit);

    /**
     * 사용자별 당첨 내역 조회
     */
    List<LotteryResult> findWinningResultsByUserId(Long userId);

    /**
     * 전체 당첨자 수 조회
     */
    Long countTotalWinners();

    /**
     * 쿠폰 타입별 1등 당첨자 조회
     */
    List<LotteryResult> findFirstPlaceWinners(CouponType couponType);

    /**
     * 추첨이 완료된 쿠폰 타입 목록 조회
     */
    List<CouponType> findCompletedLotteryCouponTypes();
}