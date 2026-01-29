package com.kidaristudio.vacationcouponlottery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 시스템 설정 엔티티
 * 전체 코인 수량, 당첨자 수 등 시스템 운영에 필요한 설정값을 관리합니다.
 * Key-Value 형태로 설정값을 저장하며, 런타임에 동적으로 변경 가능합니다.
 */
@Entity
@Table(name = "system_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SystemConfig {

    /**
     * 설정 키 (Primary Key)
     */
    @Id
    @Column(name = "config_key", length = 50)
    private String configKey;

    /**
     * 설정 값
     */
    @Column(name = "config_value", nullable = false)
    private String configValue;

    /**
     * 설정 설명
     */
    @Column(name = "description", length = 500)
    private String description;

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
     * 설정값을 정수로 반환
     */
    public Integer getIntValue() {
        try {
            return Integer.parseInt(this.configValue);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("설정값을 정수로 변환할 수 없습니다: " + this.configValue);
        }
    }

    /**
     * 설정값을 불린으로 반환
     */
    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(this.configValue);
    }

    /**
     * 설정값 업데이트
     */
    public void updateValue(String newValue) {
        this.configValue = newValue;
    }

    /**
     * 설정값 업데이트 (정수)
     */
    public void updateValue(Integer newValue) {
        this.configValue = newValue.toString();
    }

    /**
     * 설정값 업데이트 (불린)
     */
    public void updateValue(Boolean newValue) {
        this.configValue = newValue.toString();
    }

    /**
     * 시스템 설정 키 상수
     */
    public static class Keys {
        public static final String TOTAL_COINS = "TOTAL_COINS";
        public static final String REMAINING_COINS = "REMAINING_COINS";
        public static final String MAX_COINS_PER_USER = "MAX_COINS_PER_USER";
        public static final String WINNERS_PER_COUPON = "WINNERS_PER_COUPON";
    }
}