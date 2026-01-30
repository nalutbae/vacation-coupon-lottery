package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.QSystemConfig;
import com.kidaristudio.vacationcouponlottery.domain.SystemConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * 시스템 설정 Repository 커스텀 구현체
 * QueryDSL을 활용하여 원자적 설정값 변경을 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class SystemConfigRepositoryImpl implements SystemConfigRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QSystemConfig config = QSystemConfig.systemConfig;

    @Override
    public Optional<SystemConfig> findByConfigKeyWithLock(String configKey) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(config)
                        .where(config.configKey.eq(configKey))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

    @Override
    public boolean decrementRemainingCoins(int amount) {
        long updatedRows = queryFactory
                .update(config)
                .set(config.configValue, 
                     config.configValue.castToNum(Integer.class).subtract(amount).stringValue())
                .where(config.configKey.eq(SystemConfig.Keys.REMAINING_COINS)
                        .and(config.configValue.castToNum(Integer.class).goe(amount)))
                .execute();
        
        return updatedRows > 0;
    }

    @Override
    public boolean incrementRemainingCoins(int amount) {
        long updatedRows = queryFactory
                .update(config)
                .set(config.configValue, 
                     config.configValue.castToNum(Integer.class).add(amount).stringValue())
                .where(config.configKey.eq(SystemConfig.Keys.REMAINING_COINS))
                .execute();
        
        return updatedRows > 0;
    }

    @Override
    public Optional<Integer> getIntValue(String configKey) {
        String value = queryFactory
                .select(config.configValue)
                .from(config)
                .where(config.configKey.eq(configKey))
                .fetchOne();
        
        if (value == null) {
            return Optional.empty();
        }
        
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> getBooleanValue(String configKey) {
        String value = queryFactory
                .select(config.configValue)
                .from(config)
                .where(config.configKey.eq(configKey))
                .fetchOne();
        
        return value != null ? Optional.of(Boolean.parseBoolean(value)) : Optional.empty();
    }

    @Override
    public int getCurrentRemainingCoins() {
        return getIntValue(SystemConfig.Keys.REMAINING_COINS).orElse(0);
    }

    @Override
    public int getTotalCoins() {
        return getIntValue(SystemConfig.Keys.TOTAL_COINS).orElse(900);
    }

    @Override
    public int getMaxCoinsPerUser() {
        return getIntValue(SystemConfig.Keys.MAX_COINS_PER_USER).orElse(3);
    }

    @Override
    public int getWinnersPerCoupon() {
        return getIntValue(SystemConfig.Keys.WINNERS_PER_COUPON).orElse(3);
    }
}