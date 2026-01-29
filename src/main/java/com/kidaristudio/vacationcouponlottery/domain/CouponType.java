package com.kidaristudio.vacationcouponlottery.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 휴가 쿠폰 타입 열거형
 * - ONE_DAY: 1일권 휴가 쿠폰
 * - THREE_DAY: 3일권 휴가 쿠폰
 */
@Getter
@RequiredArgsConstructor
public enum CouponType {
    ONE_DAY("1일권"),
    THREE_DAY("3일권");

    private final String displayName;
}