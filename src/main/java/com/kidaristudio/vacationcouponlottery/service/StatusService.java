package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.dto.CouponEntryStatus;
import com.kidaristudio.vacationcouponlottery.dto.UserEntryStatus;

import java.util.List;

/**
 * 현황 조회 서비스 인터페이스
 * 개인/전체 응모 현황, 코인 현황 조회 기능을 정의합니다.
 */
public interface StatusService {

    /**
     * 사용자의 개인 응모 현황을 조회합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 개인 응모 현황 목록
     */
    ApiResponse<List<UserEntryStatus>> getUserEntryStatus(String phoneNumber);

    /**
     * 사용자의 응모 코인 수량을 조회합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 보유 응모 코인 수
     */
    ApiResponse<Integer> getUserCoinCount(String phoneNumber);

    /**
     * 전체 응모 현황을 조회합니다.
     * 
     * @return 쿠폰별 전체 응모 현황
     */
    ApiResponse<List<CouponEntryStatus>> getAllEntryStatus();

    /**
     * 전체 응모 코인 현황을 조회합니다.
     * 
     * @return 전체 코인 현황 (남은 코인 수, 사용자별 획득 수량 등)
     */
    ApiResponse<CoinStatusResponse> getAllCoinStatus();

    /**
     * 시스템 전체 통계를 조회합니다.
     * 
     * @return 시스템 전체 통계 정보
     */
    ApiResponse<SystemStatistics> getSystemStatistics();

    /**
     * 시스템 전체 통계 정보를 담는 DTO
     */
    class SystemStatistics {
        private final Integer totalUsers;
        private final Integer totalEntries;
        private final Integer totalCoinsDistributed;
        private final Integer remainingCoins;
        private final Boolean isLotteryCompleted;

        public SystemStatistics(Integer totalUsers, Integer totalEntries, Integer totalCoinsDistributed, 
                               Integer remainingCoins, Boolean isLotteryCompleted) {
            this.totalUsers = totalUsers;
            this.totalEntries = totalEntries;
            this.totalCoinsDistributed = totalCoinsDistributed;
            this.remainingCoins = remainingCoins;
            this.isLotteryCompleted = isLotteryCompleted;
        }

        // Getters
        public Integer getTotalUsers() { return totalUsers; }
        public Integer getTotalEntries() { return totalEntries; }
        public Integer getTotalCoinsDistributed() { return totalCoinsDistributed; }
        public Integer getRemainingCoins() { return remainingCoins; }
        public Boolean getIsLotteryCompleted() { return isLotteryCompleted; }
    }
}