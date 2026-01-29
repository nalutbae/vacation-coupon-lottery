package com.kidaristudio.vacationcouponlottery.repository;

import com.kidaristudio.vacationcouponlottery.domain.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 시스템 설정 Repository
 * 시스템 설정값 관리와 동시성 처리를 위한 락 기능을 제공합니다.
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String>, SystemConfigRepositoryCustom {

    /**
     * 설정 키로 설정값 조회
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * 설정 키 존재 여부 확인
     */
    boolean existsByConfigKey(String configKey);
}