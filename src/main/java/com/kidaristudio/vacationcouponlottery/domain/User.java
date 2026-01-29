package com.kidaristudio.vacationcouponlottery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티
 * 키다리스튜디오 사원 정보를 관리하며, 전화번호로 식별됩니다.
 * 응모 코인 수량과 휴가 쿠폰 응모 내역을 포함합니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 전화번호 (사용자 식별자)
     * 유니크 제약조건이 적용되며, 사용자 식별의 기본 키 역할
     */
    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
    private String phoneNumber;

    /**
     * 보유 응모 코인 수
     * 0~3개 범위 제한, 기본값 0
     */
    @Column(name = "coin_count", nullable = false)
    @Builder.Default
    private Integer coinCount = 0;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 휴가 쿠폰 응모 내역 (일대다 관계)
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VacationCouponEntry> entries = new ArrayList<>();

    /**
     * 응모 코인 증가
     * 최대 3개까지만 증가 가능
     */
    public void increaseCoinCount() {
        if (this.coinCount >= 3) {
            throw new IllegalStateException("응모 코인은 최대 3개까지만 보유할 수 있습니다.");
        }
        this.coinCount++;
    }

    /**
     * 응모 코인 감소
     * 0개 미만으로 감소 불가
     */
    public void decreaseCoinCount(int amount) {
        if (this.coinCount < amount) {
            throw new IllegalStateException("보유한 응모 코인이 부족합니다.");
        }
        this.coinCount -= amount;
    }

    /**
     * 응모 코인 증가 (특정 수량)
     */
    public void increaseCoinCount(int amount) {
        if (this.coinCount + amount > 3) {
            throw new IllegalStateException("응모 코인은 최대 3개까지만 보유할 수 있습니다.");
        }
        this.coinCount += amount;
    }

    /**
     * 응모 가능 여부 확인
     */
    public boolean canEnterLottery(int requiredCoins) {
        return this.coinCount >= requiredCoins;
    }
}