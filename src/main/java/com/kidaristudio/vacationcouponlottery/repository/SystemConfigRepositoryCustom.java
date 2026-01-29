package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.SystemConfig;

import java.util.Optional;

/**
 * 시스템 설정 Repository 커스텀 인터페이스
 * QueryDSL을 활용한 원자적 설정값 변경 메서드를 정의합니다.
 */
public interface SystemConfigRepositoryCustom {

    /**
     * 설정 키로 설정값 조회 (비관적 락)
     * 동시성 처리가 필요한 설정값 변경에서 사용
     */
    Optional<SystemConfig> findByConfigKeyWithLock(String configKey);

    /**
     * 남은 코인 수량을 원자적으로 감소
     * @param amount 감소할 수량
     * @return 성공 여부 (잔여량이 충분한 경우 true)
     */
    boolean decrementRemainingCoins(int amount);

    /**
     * 남은 코인 수량을 원자적으로 증가
     * @param amount 증가할 수량
     * @return 성공 여부
     */
    boolean incrementRemainingCoins(int amount);

    /**
     * 설정값을 정수로 조회
     */
    Optional<Integer> getIntValue(String configKey);

    /**
     * 설정값을 불린으로 조회
     */
    Optional<Boolean> getBooleanValue(String configKey);

    /**
     * 현재 남은 코인 수량 조회
     */
    int getCurrentRemainingCoins();

    /**
     * 전체 코인 수량 조회
     */
    int getTotalCoins();

    /**
     * 사용자당 최대 코인 수 조회
     */
    int getMaxCoinsPerUser();

    /**
     * 쿠폰당 당첨자 수 조회
     */
    int getWinnersPerCoupon();
}