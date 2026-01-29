package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 휴가 쿠폰 응모 Repository
 * 응모 내역 관리와 조회 기능을 제공합니다.
 */
@Repository
public interface VacationCouponEntryRepository extends JpaRepository<VacationCouponEntry, Long>, VacationCouponEntryRepositoryCustom {

    /**
     * 사용자별 활성 응모 내역 조회
     */
    List<VacationCouponEntry> findByUserAndIsActiveTrue(User user);

    /**
     * 사용자별 전체 응모 내역 조회
     */
    List<VacationCouponEntry> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 쿠폰 타입별 활성 응모 내역 조회
     */
    List<VacationCouponEntry> findByCouponTypeAndIsActiveTrue(CouponType couponType);

    /**
     * 사용자의 특정 쿠폰 타입 활성 응모 내역 조회
     */
    List<VacationCouponEntry> findByUserAndCouponTypeAndIsActiveTrue(User user, CouponType couponType);

    /**
     * 활성 응모 내역 전체 조회
     */
    List<VacationCouponEntry> findByIsActiveTrueOrderByCreatedAtAsc();
}