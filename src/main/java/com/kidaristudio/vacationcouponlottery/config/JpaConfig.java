package com.kidaristudio.vacationcouponlottery.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 및 QueryDSL 설정
 * - JPA Auditing 활성화 (생성/수정 시간 자동 관리)
 * - QueryDSL JPAQueryFactory 빈 등록
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.kidaristudio.vacationcouponlottery.repository")
@EnableTransactionManagement
public class JpaConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * QueryDSL JPAQueryFactory 빈 등록
     * 복잡한 동적 쿼리 작성을 위한 QueryDSL 설정
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}