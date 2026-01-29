package com.kidaristudio.vacationcouponlottery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 추첨 결과 엔티티
 * 당첨자 정보 및 추첨 결과를 저장합니다.
 * 각 응모에 대한 당첨 순위와 추첨 일시를 관리합니다.
 */
@Entity
@Table(name = "lottery_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LotteryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 당첨된 응모 (다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private VacationCouponEntry entry;

    /**
     * 쿠폰 타입 (1일권/3일권)
     * 응모의 쿠폰 타입과 동일해야 함
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 20)
    private CouponType couponType;

    /**
     * 당첨 순위 (1, 2, 3등)
     */
    @Column(name = "rank", nullable = false)
    private Integer rank;

    /**
     * 추첨 일시
     */
    @CreatedDate
    @Column(name = "lottery_date", nullable = false, updatable = false)
    private LocalDateTime lotteryDate;

    /**
     * 당첨자 전화번호 조회
     */
    public String getWinnerPhoneNumber() {
        return this.entry.getUser().getPhoneNumber();
    }

    /**
     * 사용한 응모 코인 수 조회
     */
    public Integer getUsedCoinCount() {
        return this.entry.getCoinCount();
    }

    /**
     * 1등 여부 확인
     */
    public boolean isFirstPlace() {
        return this.rank == 1;
    }

    /**
     * 2등 여부 확인
     */
    public boolean isSecondPlace() {
        return this.rank == 2;
    }

    /**
     * 3등 여부 확인
     */
    public boolean isThirdPlace() {
        return this.rank == 3;
    }

    /**
     * 순위 문자열 반환
     */
    public String getRankDisplay() {
        return switch (this.rank) {
            case 1 -> "1등";
            case 2 -> "2등";
            case 3 -> "3등";
            default -> this.rank + "등";
        };
    }
}