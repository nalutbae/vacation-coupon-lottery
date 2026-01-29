package com.kidaristudio.vacationcouponlottery.dto;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 당첨자 정보 DTO
 * 추첨 당첨자의 상세 정보를 담습니다.
 */
@Getter
@Builder
public class Winner {
    
    /**
     * 응모 ID
     */
    private final Long entryId;
    
    /**
     * 전화번호 (사용자 식별자)
     */
    private final String phoneNumber;
    
    /**
     * 쿠폰 타입
     */
    private final CouponType couponType;
    
    /**
     * 사용한 응모 코인 수
     */
    private final Integer coinCount;
    
    /**
     * 당첨 순위 (1, 2, 3)
     */
    private final Integer rank;
    
    /**
     * 응모 시간
     */
    private final LocalDateTime entryTime;
    
    /**
     * 당첨 시간
     */
    private final LocalDateTime winningTime;
}