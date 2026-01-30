package com.kidaristudio.vacationcouponlottery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 코인 정보 DTO
 * 현재 보유 코인과 누적 획득 코인 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCoinInfo {
    
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
     * 추가 코인 획득 가능 여부
     */
    private Boolean canAcquireMore;
}