package com.kidaristudio.vacationcouponlottery.dto;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 응모 현황 DTO
 * 개별 응모 내역 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntryStatus {

    /**
     * 응모 ID
     */
    private Long entryId;

    /**
     * 쿠폰 타입
     */
    private CouponType couponType;

    /**
     * 사용한 코인 수
     */
    private Integer coinCount;

    /**
     * 응모 상태 (활성/비활성)
     */
    private Boolean isActive;

    /**
     * 응모 시간
     */
    private LocalDateTime entryTime;

    /**
     * 당첨 여부
     */
    private Boolean isWinner;

    /**
     * 당첨 순위 (당첨된 경우)
     */
    private Integer winningRank;
}