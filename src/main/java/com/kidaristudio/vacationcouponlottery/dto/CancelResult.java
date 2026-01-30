package com.kidaristudio.vacationcouponlottery.dto;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 휴가 쿠폰 응모 취소 결과 DTO
 * 응모 취소 성공 시 반환되는 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelResult {

    /**
     * 취소된 응모 ID
     */
    private Long entryId;

    /**
     * 사용자 전화번호
     */
    private String phoneNumber;

    /**
     * 쿠폰 타입
     */
    private CouponType couponType;

    /**
     * 반환된 코인 수
     */
    private Integer returnedCoins;

    /**
     * 취소 후 총 코인 수
     */
    private Integer totalCoins;

    /**
     * 취소 시간
     */
    private LocalDateTime cancelTime;
}