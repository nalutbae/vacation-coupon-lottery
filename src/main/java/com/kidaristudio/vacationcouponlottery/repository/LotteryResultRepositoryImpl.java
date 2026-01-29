package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.LotteryResult;
import com.kidaristudio.vacationcouponlottery.domain.QLotteryResult;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 추첨 결과 Repository 커스텀 구현체
 * QueryDSL을 활용하여 복잡한 추첨 결과 조회를 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class LotteryResultRepositoryImpl implements LotteryResultRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QLotteryResult result = QLotteryResult.lotteryResult;

    @Override
    public Map<CouponType, Long> countWinnersByCouponType() {
        List<Tuple> results = queryFactory
                .select(result.couponType, result.count())
                .from(result)
                .groupBy(result.couponType)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(result.couponType),
                        tuple -> tuple.get(result.count())
                ));
    }

    @Override
    public Map<Integer, Long> countWinnersByRank() {
        List<Tuple> results = queryFactory
                .select(result.rank, result.count())
                .from(result)
                .groupBy(result.rank)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(result.rank),
                        tuple -> tuple.get(result.count())
                ));
    }

    @Override
    public List<LotteryResult> findResultsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .selectFrom(result)
                .where(result.lotteryDate.between(startDate, endDate))
                .orderBy(result.lotteryDate.desc(), result.couponType.asc(), result.rank.asc())
                .fetch();
    }

    @Override
    public List<LotteryResult> findRecentResults(int limit) {
        return queryFactory
                .selectFrom(result)
                .orderBy(result.lotteryDate.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<LotteryResult> findWinningResultsByUserId(Long userId) {
        return queryFactory
                .selectFrom(result)
                .where(result.entry.user.id.eq(userId))
                .orderBy(result.lotteryDate.desc(), result.rank.asc())
                .fetch();
    }

    @Override
    public Long countTotalWinners() {
        return queryFactory
                .select(result.count())
                .from(result)
                .fetchOne();
    }

    @Override
    public List<LotteryResult> findFirstPlaceWinners(CouponType couponType) {
        return queryFactory
                .selectFrom(result)
                .where(result.couponType.eq(couponType)
                        .and(result.rank.eq(1)))
                .orderBy(result.lotteryDate.desc())
                .fetch();
    }

    @Override
    public List<CouponType> findCompletedLotteryCouponTypes() {
        return queryFactory
                .select(result.couponType)
                .from(result)
                .groupBy(result.couponType)
                .fetch();
    }
}