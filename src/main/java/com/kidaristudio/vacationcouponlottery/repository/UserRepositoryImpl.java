package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.QUser;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository 커스텀 구현체
 * QueryDSL을 활용하여 복잡한 쿼리를 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUser user = QUser.user;

    @Override
    public Optional<User> findByPhoneNumberWithLock(String phoneNumber) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(user)
                        .where(user.phoneNumber.eq(phoneNumber))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

    @Override
    public List<User> findUsersByCoinCount(int coinCount) {
        return queryFactory
                .selectFrom(user)
                .where(user.coinCount.eq(coinCount))
                .orderBy(user.createdAt.asc())
                .fetch();
    }

    @Override
    public List<User> findUsersByCoinCountBetween(int minCoins, int maxCoins) {
        return queryFactory
                .selectFrom(user)
                .where(user.coinCount.between(minCoins, maxCoins))
                .orderBy(user.coinCount.desc(), user.createdAt.asc())
                .fetch();
    }

    @Override
    public Long getTotalCoinCount() {
        Integer result = queryFactory
                .select(user.coinCount.sum().coalesce(0))
                .from(user)
                .fetchOne();
        return result != null ? result.longValue() : 0L;
    }

    @Override
    public Long countUsersWithCoins() {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(user.coinCount.gt(0))
                .fetchOne();
    }

    @Override
    public List<User> findUsersWithMaxCoins() {
        return queryFactory
                .selectFrom(user)
                .where(user.coinCount.eq(3))
                .orderBy(user.createdAt.asc())
                .fetch();
    }
}