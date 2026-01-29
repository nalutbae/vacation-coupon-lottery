package com.kidaristudio.vacationcouponlottery.dto;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쿠폰별 전체 응모 현황 DTO
 * 특정 쿠폰 타입의 전체 응모 통계를 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponEntryStatus {

    /**
     * 쿠폰 타입
     */
    private CouponType couponType;

    /**
     * 총 응모자 수 (활성 응모만)
     */
    private Long totalEntrants;

    /**
     * 총 응모 코인 수 (활성 응모만)
     */
    private Long totalCoins;

    /**
     * 평균 응모 코인 수
     */
    private Double averageCoins;

    /**
     * 최대 응모 코인 수
     */
    private Integer maxCoins;

    /**
     * 최소 응모 코인 수
     */
    private Integer minCoins;

    /**
     * 추첨 완료 여부
     */
    private Boolean isLotteryCompleted;

    /**
     * 당첨자 수
     */
    private Integer winnerCount;
}