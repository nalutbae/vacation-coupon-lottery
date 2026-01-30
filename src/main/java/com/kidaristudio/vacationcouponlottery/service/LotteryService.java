package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.LotteryResult;
import com.kidaristudio.vacationcouponlottery.dto.Winner;

import java.util.List;

/**
 * 추첨 서비스 인터페이스
 * 휴가 쿠폰 추첨 관련 비즈니스 로직을 정의합니다.
 */
public interface LotteryService {

    /**
     * 특정 쿠폰 타입에 대한 추첨을 실행합니다.
     * 
     * @param couponType 추첨할 쿠폰 타입
     * @return 추첨 결과
     */
    ApiResponse<LotteryResult> conductLottery(CouponType couponType);

    /**
     * 모든 쿠폰 타입에 대한 일괄 추첨을 실행합니다.
     * 
     * @return 전체 추첨 결과 목록
     */
    ApiResponse<List<LotteryResult>> conductAllLotteries();

    /**
     * 특정 쿠폰 타입의 당첨자 목록을 조회합니다.
     * 
     * @param couponType 조회할 쿠폰 타입 (null인 경우 전체 조회)
     * @return 당첨자 목록
     */
    ApiResponse<List<Winner>> getWinners(CouponType couponType);

    /**
     * 추첨 완료 여부를 확인합니다.
     * 
     * @param couponType 확인할 쿠폰 타입 (null인 경우 전체 확인)
     * @return 추첨 완료 여부
     */
    ApiResponse<Boolean> isLotteryCompleted(CouponType couponType);

    /**
     * 추첨 결과를 초기화합니다. (개발/테스트 용도)
     * 
     * @param couponType 초기화할 쿠폰 타입 (null인 경우 전체 초기화)
     * @return 초기화 결과
     */
    ApiResponse<Void> resetLottery(CouponType couponType);
}