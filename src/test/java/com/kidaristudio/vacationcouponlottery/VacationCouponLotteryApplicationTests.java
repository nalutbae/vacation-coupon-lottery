package com.kidaristudio.vacationcouponlottery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 애플리케이션 기본 설정 테스트
 * SpringBoot 컨텍스트 로딩 및 기본 설정 검증
 */
@SpringBootTest
@ActiveProfiles("dev")
class VacationCouponLotteryApplicationTests {

    @Test
    void contextLoads() {
        // SpringBoot 컨텍스트가 정상적으로 로드되는지 확인
        // 이 테스트는 애플리케이션의 기본 설정이 올바른지 검증
        assertThat(Boolean.TRUE).isTrue();
    }
}