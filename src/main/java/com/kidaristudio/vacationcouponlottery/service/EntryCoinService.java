package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;

/**
 * 응모 코인 관리 서비스 인터페이스
 * 코인 획득, 수량 조회, 전체 현황 조회 기능을 제공합니다.
 * 동시성 처리와 트랜잭션 관리를 포함합니다.
 */
public interface EntryCoinService {

    /**
     * 응모 코인 획득
     * 선착순으로 응모 코인 1개를 제공합니다.
     * 동시성 처리를 통해 안전한 코인 분배를 보장합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 코인 획득 결과
     * @throws com.kidaristudio.vacationcouponlottery.exception.CoinException.CoinLimitExceededException 사용자가 이미 3개 보유
     * @throws com.kidaristudio.vacationcouponlottery.exception.CoinException.NoRemainingCoinsException 전체 코인 소진
     * @throws com.kidaristudio.vacationcouponlottery.exception.CoinException.CoinAcquisitionFailedException 획득 실패
     */
    ApiResponse<CoinAcquisitionResult> acquireCoin(String phoneNumber);

    /**
     * 사용자 응모 코인 수량 조회
     * 특정 사용자의 현재 보유 코인 수를 반환합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 보유 코인 수
     */
    ApiResponse<Integer> getUserCoinCount(String phoneNumber);

    /**
     * 전체 응모 코인 현황 조회
     * 시스템 전체의 코인 분배 현황을 제공합니다.
     * 
     * @return 전체 코인 현황
     */
    ApiResponse<CoinStatusResponse> getAllCoinStatus();

    /**
     * 사용자 코인 차감
     * 휴가 쿠폰 응모 시 사용되는 내부 메서드입니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @param amount 차감할 코인 수
     * @throws com.kidaristudio.vacationcouponlottery.exception.CoinException.InsufficientCoinsException 코인 부족
     */
    void deductCoins(String phoneNumber, int amount);

    /**
     * 사용자 코인 반환
     * 휴가 쿠폰 응모 취소 시 사용되는 내부 메서드입니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @param amount 반환할 코인 수
     * @throws com.kidaristudio.vacationcouponlottery.exception.CoinException.CoinLimitExceededException 한도 초과
     */
    void returnCoins(String phoneNumber, int amount);
}