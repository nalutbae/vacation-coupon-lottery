package com.kidaristudio.vacationcouponlottery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 휴가 쿠폰 응모 엔티티
 * 사용자의 1일권/3일권 휴가 쿠폰 응모 내역을 관리합니다.
 * 응모 시 사용한 코인 수와 응모 상태를 포함합니다.
 */
@Entity
@Table(name = "vacation_coupon_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class VacationCouponEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 응모한 사용자 (다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 쿠폰 타입 (1일권/3일권)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 20)
    private CouponType couponType;

    /**
     * 사용한 응모 코인 수
     * 1개 이상이어야 함
     */
    @Column(name = "coin_count", nullable = false)
    private Integer coinCount;

    /**
     * 응모 활성 상태
     * true: 활성 응모, false: 취소된 응모
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 응모 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 추첨 결과 (일대다 관계)
     */
    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LotteryResult> lotteryResults = new ArrayList<>();

    /**
     * 응모 취소
     * 활성 상태를 false로 변경
     */
    public void cancel() {
        if (!this.isActive) {
            throw new IllegalStateException("이미 취소된 응모입니다.");
        }
        this.isActive = false;
    }

    /**
     * 응모 활성화
     * 취소된 응모를 다시 활성화
     */
    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("이미 활성화된 응모입니다.");
        }
        this.isActive = true;
    }

    /**
     * 당첨 여부 확인
     */
    public boolean isWinner() {
        return !lotteryResults.isEmpty();
    }

    /**
     * 당첨 순위 조회
     * 당첨되지 않은 경우 null 반환
     */
    public Integer getWinningRank() {
        return lotteryResults.stream()
                .findFirst()
                .map(LotteryResult::getRank)
                .orElse(null);
    }
}