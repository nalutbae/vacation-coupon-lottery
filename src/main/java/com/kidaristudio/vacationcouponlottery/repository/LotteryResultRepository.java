package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.LotteryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 추첨 결과 Repository
 * 당첨자 정보와 추첨 결과 관리 기능을 제공합니다.
 */
@Repository
public interface LotteryResultRepository extends JpaRepository<LotteryResult, Long>, LotteryResultRepositoryCustom {

    /**
     * 쿠폰 타입별 추첨 결과 조회 (순위 순)
     */
    List<LotteryResult> findByCouponTypeOrderByRankAsc(CouponType couponType);

    /**
     * 쿠폰 타입별 추첨 결과 조회 (순위 순) - 서비스용 별칭
     */
    default List<LotteryResult> findByCouponTypeOrderByRank(CouponType couponType) {
        return findByCouponTypeOrderByRankAsc(couponType);
    }

    /**
     * 전체 추첨 결과 조회 (쿠폰 타입, 순위 순)
     */
    List<LotteryResult> findAllByOrderByCouponTypeAscRankAsc();

    /**
     * 전체 추첨 결과 조회 (쿠폰 타입, 순위 순) - 서비스용 별칭
     */
    default List<LotteryResult> findAllOrderByCouponTypeAndRank() {
        return findAllByOrderByCouponTypeAscRankAsc();
    }

    /**
     * 특정 순위의 당첨자 조회
     */
    List<LotteryResult> findByRankOrderByCouponTypeAsc(Integer rank);

    /**
     * 쿠폰 타입별 추첨 완료 여부 확인
     */
    boolean existsByCouponType(CouponType couponType);

    /**
     * 특정 응모의 추첨 결과 조회
     */
    List<LotteryResult> findByEntryId(Long entryId);

    /**
     * 쿠폰 타입별 추첨 결과 삭제
     */
    void deleteByCouponType(CouponType couponType);
}