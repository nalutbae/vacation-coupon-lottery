package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.QVacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 휴가 쿠폰 응모 Repository 커스텀 구현체
 * QueryDSL을 활용하여 복잡한 집계 쿼리를 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class VacationCouponEntryRepositoryImpl implements VacationCouponEntryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QVacationCouponEntry entry = QVacationCouponEntry.vacationCouponEntry;

    @Override
    public Map<CouponType, Long> countActiveEntriesByCouponType() {
        List<Tuple> results = queryFactory
                .select(entry.couponType, entry.count())
                .from(entry)
                .where(entry.isActive.isTrue())
                .groupBy(entry.couponType)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(entry.couponType),
                        tuple -> tuple.get(entry.count())
                ));
    }

    @Override
    public Map<CouponType, Long> sumCoinsByCouponType() {
        List<Tuple> results = queryFactory
                .select(entry.couponType, entry.coinCount.sum().coalesce(0))
                .from(entry)
                .where(entry.isActive.isTrue())
                .groupBy(entry.couponType)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(entry.couponType),
                        tuple -> tuple.get(entry.coinCount.sum().coalesce(0)).longValue()
                ));
    }

    @Override
    public Long getTotalUsedCoinsByUser(Long userId) {
        Integer result = queryFactory
                .select(entry.coinCount.sum().coalesce(0))
                .from(entry)
                .where(entry.user.id.eq(userId)
                        .and(entry.isActive.isTrue()))
                .fetchOne();
        return result != null ? result.longValue() : 0L;
    }

    @Override
    public List<VacationCouponEntry> findLotteryTargetEntries(CouponType couponType) {
        return queryFactory
                .selectFrom(entry)
                .where(entry.couponType.eq(couponType)
                        .and(entry.isActive.isTrue()))
                .orderBy(entry.createdAt.asc())
                .fetch();
    }

    @Override
    public Long countActiveEntries() {
        return queryFactory
                .select(entry.count())
                .from(entry)
                .where(entry.isActive.isTrue())
                .fetchOne();
    }

    @Override
    public Long countActiveEntriesByUser(Long userId) {
        return queryFactory
                .select(entry.count())
                .from(entry)
                .where(entry.user.id.eq(userId)
                        .and(entry.isActive.isTrue()))
                .fetchOne();
    }

    @Override
    public Map<Integer, Long> countEntriesByCoinCount() {
        List<Tuple> results = queryFactory
                .select(entry.coinCount, entry.count())
                .from(entry)
                .where(entry.isActive.isTrue())
                .groupBy(entry.coinCount)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(entry.coinCount),
                        tuple -> tuple.get(entry.count())
                ));
    }
}