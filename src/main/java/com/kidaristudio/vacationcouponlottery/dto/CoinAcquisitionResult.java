package com.kidaristudio.vacationcouponlottery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 응모 코인 획득 결과 DTO
 * 코인 획득 후 사용자의 현재 상태를 반환합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinAcquisitionResult {
    
    /**
     * 사용자 전화번호
     */
    private String phoneNumber;
    
    /**
     * 현재 보유 코인 수
     */
    private Integer coinCount;
    
    /**
     * 누적 획득 코인 수
     */
    private Integer totalAcquiredCoins;
    
    /**
     * 획득한 코인 수 (일반적으로 1개)
     */
    private Integer acquiredCoins;
    
    /**
     * 전체 남은 코인 수
     */
    private Integer remainingCoins;
}