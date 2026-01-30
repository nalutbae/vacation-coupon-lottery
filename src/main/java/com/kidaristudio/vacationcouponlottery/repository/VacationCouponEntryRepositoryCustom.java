package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;

import java.util.List;
import java.util.Map;

/**
 * 휴가 쿠폰 응모 Repository 커스텀 인터페이스
 * QueryDSL을 활용한 복잡한 집계 쿼리를 정의합니다.
 */
public interface VacationCouponEntryRepositoryCustom {

    /**
     * 쿠폰 타입별 활성 응모 수 집계
     */
    Map<CouponType, Long> countActiveEntriesByCouponType();

    /**
     * 쿠폰 타입별 총 사용된 코인 수 집계
     */
    Map<CouponType, Long> sumCoinsByCouponType();

    /**
     * 사용자별 총 사용한 코인 수 조회
     */
    Long getTotalUsedCoinsByUser(Long userId);

    /**
     * 특정 쿠폰 타입의 추첨 대상 응모 목록 조회 (활성 응모만)
     */
    List<VacationCouponEntry> findLotteryTargetEntries(CouponType couponType);

    /**
     * 전체 활성 응모 수 조회
     */
    Long countActiveEntries();

    /**
     * 사용자별 활성 응모 수 조회
     */
    Long countActiveEntriesByUser(Long userId);

    /**
     * 코인 수별 응모 내역 조회 (통계용)
     */
    Map<Integer, Long> countEntriesByCoinCount();
}