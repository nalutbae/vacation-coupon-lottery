package com.kidaristudio.vacationcouponlottery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 전체 응모 코인 현황 응답 DTO
 * 시스템 전체의 코인 분배 현황을 제공합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinStatusResponse {
    
    /**
     * 전체 코인 수량
     */
    private Integer totalCoins;
    
    /**
     * 남은 코인 수량
     */
    private Integer remainingCoins;
    
    /**
     * 분배된 코인 수량
     */
    private Integer distributedCoins;
    
    /**
     * 코인을 보유한 사용자 수
     */
    private Long usersWithCoins;
    
    /**
     * 최대 코인을 보유한 사용자 수 (3개)
     */
    private Long usersWithMaxCoins;
    
    /**
     * 사용자별 코인 분배 현황
     */
    private List<UserCoinStatus> userCoinStatuses;
    
    /**
     * 사용자별 코인 현황 내부 클래스
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserCoinStatus {
        private String phoneNumber;
        private Integer coinCount;
    }
}