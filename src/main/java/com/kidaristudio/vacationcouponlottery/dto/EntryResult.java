package com.kidaristudio.vacationcouponlottery.dto;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 휴가 쿠폰 응모 결과 DTO
 * 응모 성공 시 반환되는 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryResult {

    /**
     * 응모 ID
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
     * 사용한 코인 수
     */
    private Integer usedCoins;

    /**
     * 응모 후 남은 코인 수
     */
    private Integer remainingCoins;

    /**
     * 응모 시간
     */
    private LocalDateTime entryTime;

    /**
     * 응모 상태 (활성/비활성)
     */
    private Boolean isActive;
}