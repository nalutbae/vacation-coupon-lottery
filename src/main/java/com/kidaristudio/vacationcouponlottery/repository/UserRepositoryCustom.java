package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository 커스텀 인터페이스
 * QueryDSL을 활용한 복잡한 쿼리 메서드를 정의합니다.
 */
public interface UserRepositoryCustom {

    /**
     * 전화번호로 사용자 조회 (비관적 락)
     * 동시성 처리가 필요한 코인 관련 연산에서 사용
     */
    Optional<User> findByPhoneNumberWithLock(String phoneNumber);

    /**
     * 특정 코인 수를 보유한 사용자 목록 조회
     */
    List<User> findUsersByCoinCount(int coinCount);

    /**
     * 코인 수 범위로 사용자 목록 조회
     */
    List<User> findUsersByCoinCountBetween(int minCoins, int maxCoins);

    /**
     * 전체 사용자의 코인 수 합계 조회
     */
    Long getTotalCoinCount();

    /**
     * 코인을 보유한 사용자 수 조회
     */
    Long countUsersWithCoins();

    /**
     * 최대 코인을 보유한 사용자 목록 조회 (3개)
     */
    List<User> findUsersWithMaxCoins();
}