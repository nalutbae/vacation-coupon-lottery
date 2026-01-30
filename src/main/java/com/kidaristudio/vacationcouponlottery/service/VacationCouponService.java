package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.*;

import java.util.List;

/**
 * 휴가 쿠폰 응모 서비스 인터페이스
 * 휴가 쿠폰 응모, 취소, 현황 조회 기능을 제공합니다.
 */
public interface VacationCouponService {

    /**
     * 휴가 쿠폰 응모
     * 사용자의 응모 코인을 차감하고 해당 쿠폰에 응모를 등록합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @param couponType 쿠폰 타입 (1일권/3일권)
     * @param coinCount 사용할 응모 코인 수
     * @return 응모 결과
     */
    ApiResponse<EntryResult> enterLottery(String phoneNumber, CouponType couponType, int coinCount);

    /**
     * 휴가 쿠폰 응모 취소
     * 응모를 취소하고 사용한 응모 코인을 반환합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @param entryId 취소할 응모 ID
     * @return 취소 결과
     */
    ApiResponse<CancelResult> cancelEntry(String phoneNumber, Long entryId);

    /**
     * 사용자 응모 현황 조회
     * 특정 사용자의 모든 응모 내역을 조회합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 사용자 응모 현황 목록
     */
    ApiResponse<List<UserEntryStatus>> getUserEntries(String phoneNumber);

    /**
     * 휴가 쿠폰별 전체 응모 현황 조회
     * 모든 쿠폰 타입의 전체 응모 통계를 조회합니다.
     * 
     * @return 쿠폰별 응모 현황 목록
     */
    ApiResponse<List<CouponEntryStatus>> getAllEntries();
}