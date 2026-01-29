package com.kidaristudio.vacationcouponlottery.dto;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 추첨 결과 DTO
 * 추첨 실행 결과 정보를 담습니다.
 */
@Getter
@Builder
public class LotteryResult {
    
    /**
     * 쿠폰 타입
     */
    private final CouponType couponType;
    
    /**
     * 총 응모자 수
     */
    private final Integer totalEntrants;
    
    /**
     * 총 응모 코인 수
     */
    private final Integer totalCoins;
    
    /**
     * 당첨자 수
     */
    private final Integer winnerCount;
    
    /**
     * 당첨자 목록
     */
    private final List<Winner> winners;
    
    /**
     * 추첨 실행 시간
     */
    private final LocalDateTime lotteryTime;
    
    /**
     * 추첨 성공 여부
     */
    private final Boolean isSuccess;
    
    /**
     * 추첨 메시지
     */
    private final String message;
}