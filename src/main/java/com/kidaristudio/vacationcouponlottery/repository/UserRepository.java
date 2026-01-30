package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 * 기본적인 CRUD 연산과 전화번호 기반 조회를 제공합니다.
 * 동시성 처리를 위한 락 메서드를 포함합니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    /**
     * 전화번호로 사용자 조회
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * 전화번호 존재 여부 확인
     */
    boolean existsByPhoneNumber(String phoneNumber);
}