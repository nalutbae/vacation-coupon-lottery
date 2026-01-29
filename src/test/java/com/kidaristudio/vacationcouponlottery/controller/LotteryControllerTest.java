package com.kidaristudio.vacationcouponlottery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.LotteryResult;
import com.kidaristudio.vacationcouponlottery.dto.Winner;
import com.kidaristudio.vacationcouponlottery.exception.LotteryException;
import com.kidaristudio.vacationcouponlottery.service.LotteryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LotteryController 통합 테스트
 * 추첨 시스템 API의 동작을 검증합니다.
 */
@WebMvcTest(LotteryController.class)
class LotteryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LotteryService lotteryService;

    @Test
    @DisplayName("특정 쿠폰 타입 추첨 실행 API - 성공")
    void conductLottery_Success() throws Exception {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        
        List<Winner> winners = Arrays.asList(
                Winner.builder()
                        .entryId(1L)
                        .phoneNumber("010-1111-1111")
                        .couponType(CouponType.ONE_DAY)
                        .coinCount(2)
                        .rank(1)
                        .entryTime(LocalDateTime.now())
                        .winningTime(LocalDateTime.now())
                        .build(),
                Winner.builder()
                        .entryId(2L)
                        .phoneNumber("010-2222-2222")
                        .couponType(CouponType.ONE_DAY)
                        .coinCount(1)
                        .rank(2)
                        .entryTime(LocalDateTime.now())
                        .winningTime(LocalDateTime.now())
                        .build(),
                Winner.builder()
                        .entryId(3L)
                        .phoneNumber("010-3333-3333")
                        .couponType(CouponType.ONE_DAY)
                        .coinCount(3)
                        .rank(3)
                        .entryTime(LocalDateTime.now())
                        .winningTime(LocalDateTime.now())
                        .build()
        );
        
        LotteryResult result = LotteryResult.builder()
                .couponType(couponType)
                .totalEntrants(10)
                .totalCoins(20)
                .winnerCount(3)
                .winners(winners)
                .lotteryTime(LocalDateTime.now())
                .isSuccess(true)
                .message("추첨이 완료되었습니다")
                .build();
        ApiResponse<LotteryResult> response = ApiResponse.success("추첨이 완료되었습니다", result);
        
        given(lotteryService.conductLottery(couponType)).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/lottery/conduct")
                        .param("couponType", couponType.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("추첨이 완료되었습니다"))
                .andExpect(jsonPath("$.data.couponType").value("ONE_DAY"))
                .andExpect(jsonPath("$.data.winners").isArray())
                .andExpect(jsonPath("$.data.winners.length()").value(3))
                .andExpect(jsonPath("$.data.winners[0].phoneNumber").value("010-1111-1111"))
                .andExpect(jsonPath("$.data.winners[0].rank").value(1))
                .andExpect(jsonPath("$.data.winners[1].phoneNumber").value("010-2222-2222"))
                .andExpect(jsonPath("$.data.winners[1].rank").value(2))
                .andExpect(jsonPath("$.data.winners[2].phoneNumber").value("010-3333-3333"))
                .andExpect(jsonPath("$.data.winners[2].rank").value(3));
    }

    @Test
    @DisplayName("특정 쿠폰 타입 추첨 실행 API - 이미 완료된 추첨")
    void conductLottery_AlreadyCompleted() throws Exception {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        
        given(lotteryService.conductLottery(couponType))
                .willThrow(new LotteryException.LotteryAlreadyCompletedException("1일권"));

        // When & Then
        mockMvc.perform(post("/api/lottery/conduct")
                        .param("couponType", couponType.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LOTTERY_ALREADY_COMPLETED"));
    }

    @Test
    @DisplayName("특정 쿠폰 타입 추첨 실행 API - 응모자 없음")
    void conductLottery_NoEntrants() throws Exception {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        
        given(lotteryService.conductLottery(couponType))
                .willThrow(new LotteryException.NoEntrantsException("1일권"));

        // When & Then
        mockMvc.perform(post("/api/lottery/conduct")
                        .param("couponType", couponType.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NO_ENTRANTS"));
    }

    @Test
    @DisplayName("전체 쿠폰 타입 일괄 추첨 실행 API - 성공")
    void conductAllLotteries_Success() throws Exception {
        // Given
        // 나머지 테스트들은 간단한 구조로 수정
        List<Winner> winners = Arrays.asList(
                Winner.builder().phoneNumber("010-1111-1111").couponType(CouponType.ONE_DAY).rank(1).build()
        );
        
        List<LotteryResult> results = Arrays.asList(
                LotteryResult.builder().couponType(CouponType.ONE_DAY).winners(winners).build()
        );
        
        ApiResponse<List<LotteryResult>> response = ApiResponse.success("전체 추첨이 완료되었습니다", results);
        
        given(lotteryService.conductAllLotteries()).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/lottery/conduct-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("전체 추첨이 완료되었습니다"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].couponType").value("ONE_DAY"));
    }

    @Test
    @DisplayName("당첨자 조회 API - 특정 쿠폰 타입")
    void getWinners_SpecificCouponType() throws Exception {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        
        List<Winner> winners = Arrays.asList(
                Winner.builder().phoneNumber("010-1111-1111").couponType(CouponType.ONE_DAY).rank(1).build()
        );
        
        ApiResponse<List<Winner>> response = ApiResponse.success(winners);
        
        given(lotteryService.getWinners(couponType)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/lottery/winners")
                        .param("couponType", couponType.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].phoneNumber").value("010-1111-1111"));
    }

    @Test
    @DisplayName("당첨자 조회 API - 전체 조회")
    void getWinners_All() throws Exception {
        // Given
        List<Winner> allWinners = Arrays.asList(
                Winner.builder().phoneNumber("010-1111-1111").couponType(CouponType.ONE_DAY).rank(1).build()
        );
        
        ApiResponse<List<Winner>> response = ApiResponse.success(allWinners);
        
        given(lotteryService.getWinners(null)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/lottery/winners")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("추첨 완료 여부 확인 API - 특정 쿠폰 타입")
    void isLotteryCompleted_SpecificCouponType() throws Exception {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        ApiResponse<Boolean> response = ApiResponse.success(true);
        
        given(lotteryService.isLotteryCompleted(couponType)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/lottery/status")
                        .param("couponType", couponType.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("추첨 완료 여부 확인 API - 전체 확인")
    void isLotteryCompleted_All() throws Exception {
        // Given
        ApiResponse<Boolean> response = ApiResponse.success(false);
        
        given(lotteryService.isLotteryCompleted(null)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/lottery/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("추첨 결과 초기화 API - 성공")
    void resetLottery_Success() throws Exception {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        ApiResponse<Void> response = ApiResponse.success("추첨 결과가 초기화되었습니다", null);
        
        given(lotteryService.resetLottery(couponType)).willReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/lottery/reset")
                        .param("couponType", couponType.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("추첨 결과가 초기화되었습니다"));
    }

    @Test
    @DisplayName("추첨 결과 초기화 API - 전체 초기화")
    void resetLottery_All() throws Exception {
        // Given
        ApiResponse<Void> response = ApiResponse.success("모든 추첨 결과가 초기화되었습니다", null);
        
        given(lotteryService.resetLottery(null)).willReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/lottery/reset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("모든 추첨 결과가 초기화되었습니다"));
    }
}