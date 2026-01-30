package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API 통합 테스트
 * 전체 API 엔드포인트의 실제 동작을 검증합니다.
 * 실제 서비스 계층과 데이터베이스를 사용하여 엔드투엔드 테스트를 수행합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("전체 API 플로우 통합 테스트 - 코인 획득부터 추첨까지")
    void fullApiFlow_Success() throws Exception {
        String phoneNumber1 = "010-1111-1111";
        String phoneNumber2 = "010-2222-2222";
        String phoneNumber3 = "010-3333-3333";

        // 1. 코인 획득
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.coinCount").value(1));

        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.coinCount").value(1));

        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.coinCount").value(1));

        // 2. 코인 수량 확인
        mockMvc.perform(get("/api/coins/count")
                        .param("phoneNumber", phoneNumber1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(1));

        // 3. 휴가 쿠폰 응모
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber1)
                        .param("couponType", CouponType.ONE_DAY.name())
                        .param("coinCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.couponType").value("ONE_DAY"));

        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber2)
                        .param("couponType", CouponType.ONE_DAY.name())
                        .param("coinCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber3)
                        .param("couponType", CouponType.THREE_DAY.name())
                        .param("coinCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // 4. 사용자 응모 현황 조회
        mockMvc.perform(get("/api/coupons/my-entries")
                        .param("phoneNumber", phoneNumber1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].couponType").value("ONE_DAY"));

        // 5. 전체 응모 현황 조회
        mockMvc.perform(get("/api/coupons/all-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());

        // 6. 추첨 완료 여부 확인 (아직 미완료)
        mockMvc.perform(get("/api/lottery/status")
                        .param("couponType", CouponType.ONE_DAY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(false));

        // 7. 추첨 실행
        mockMvc.perform(post("/api/lottery/conduct")
                        .param("couponType", CouponType.ONE_DAY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.couponType").value("ONE_DAY"))
                .andExpect(jsonPath("$.data.winners").isArray());

        // 8. 당첨자 조회
        mockMvc.perform(get("/api/lottery/winners")
                        .param("couponType", CouponType.ONE_DAY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());

        // 9. 추첨 완료 여부 확인 (완료됨)
        mockMvc.perform(get("/api/lottery/status")
                        .param("couponType", CouponType.ONE_DAY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(true));

        // 10. 시스템 전체 통계 조회
        mockMvc.perform(get("/api/status/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalUsers").exists())
                .andExpect(jsonPath("$.data.totalEntries").exists())
                .andExpect(jsonPath("$.data.totalCoinsDistributed").exists())
                .andExpect(jsonPath("$.data.remainingCoins").exists());
    }

    @Test
    @DisplayName("응모 취소 플로우 통합 테스트")
    void entryCancelFlow_Success() throws Exception {
        String phoneNumber = "010-1234-5678";

        // 1. 코인 획득
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // 2. 휴가 쿠폰 응모
        String entryResponse = mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber)
                        .param("couponType", CouponType.ONE_DAY.name())
                        .param("coinCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.entryId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // JSON 파싱을 통해 entryId 추출
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(entryResponse);
        Long entryId = jsonNode.get("data").get("entryId").asLong();

        // 3. 코인 수량 확인 (응모 후 0개)
        mockMvc.perform(get("/api/coins/count")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(0));

        // 4. 응모 취소
        mockMvc.perform(delete("/api/coupons/cancel")
                        .param("phoneNumber", phoneNumber)
                        .param("entryId", entryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // 5. 코인 수량 확인 (취소 후 1개 복구)
        mockMvc.perform(get("/api/coins/count")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("코인 한도 초과 테스트")
    void coinLimitExceeded_Test() throws Exception {
        String phoneNumber = "010-9999-9999";

        // 1. 3개 코인 획득 (한도)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/coins/acquire")
                            .param("phoneNumber", phoneNumber))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"));
        }

        // 2. 4번째 코인 획득 시도 (한도 초과)
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COIN_LIMIT_EXCEEDED"));

        // 3. 코인 수량 확인 (3개 유지)
        mockMvc.perform(get("/api/coins/count")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    @DisplayName("코인 부족 시 응모 실패 테스트")
    void insufficientCoins_EntryFail() throws Exception {
        String phoneNumber = "010-8888-8888";

        // 1. 사용자 생성 (코인 1개 획득)
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // 2. 코인 1개로 1개 쿠폰 응모 (성공)
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber)
                        .param("couponType", CouponType.ONE_DAY.name())
                        .param("coinCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // 3. 코인이 없는 상태에서 추가 응모 시도 (실패)
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber)
                        .param("couponType", CouponType.ONE_DAY.name())
                        .param("coinCount", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_COINS"));
    }

    @Test
    @DisplayName("잘못된 파라미터 검증 테스트")
    void invalidParameters_ValidationFail() throws Exception {
        // 1. 잘못된 전화번호 형식
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "invalid-phone"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        // 2. 음수 코인 수
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", "010-1234-5678")
                        .param("couponType", CouponType.ONE_DAY.name())
                        .param("coinCount", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        // 3. 필수 파라미터 누락
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", "010-1234-5678")
                        .param("coinCount", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
    }
}