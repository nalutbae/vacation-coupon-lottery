package com.kidaristudio.vacationcouponlottery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.*;
import com.kidaristudio.vacationcouponlottery.exception.CoinException;
import com.kidaristudio.vacationcouponlottery.exception.EntryException;
import com.kidaristudio.vacationcouponlottery.service.VacationCouponService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * VacationCouponController 통합 테스트
 * 휴가 쿠폰 응모 API의 동작을 검증합니다.
 */
@WebMvcTest(VacationCouponController.class)
class VacationCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VacationCouponService vacationCouponService;

    @Test
    @DisplayName("휴가 쿠폰 응모 API - 성공")
    void enterLottery_Success() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        CouponType couponType = CouponType.ONE_DAY;
        int coinCount = 2;
        
        EntryResult result = EntryResult.builder()
                .entryId(1L)
                .phoneNumber(phoneNumber)
                .couponType(couponType)
                .usedCoins(coinCount)
                .remainingCoins(1)
                .entryTime(LocalDateTime.now())
                .isActive(true)
                .build();
        ApiResponse<EntryResult> response = ApiResponse.success("휴가 쿠폰 응모가 완료되었습니다", result);
        
        given(vacationCouponService.enterLottery(phoneNumber, couponType, coinCount)).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber)
                        .param("couponType", couponType.name())
                        .param("coinCount", String.valueOf(coinCount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("휴가 쿠폰 응모가 완료되었습니다"))
                .andExpect(jsonPath("$.data.entryId").value(1))
                .andExpect(jsonPath("$.data.phoneNumber").value(phoneNumber))
                .andExpect(jsonPath("$.data.couponType").value("ONE_DAY"))
                .andExpect(jsonPath("$.data.usedCoins").value(coinCount));
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 API - 코인 부족 예외")
    void enterLottery_InsufficientCoins() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        CouponType couponType = CouponType.ONE_DAY;
        int coinCount = 2;
        
        given(vacationCouponService.enterLottery(phoneNumber, couponType, coinCount))
                .willThrow(new CoinException.InsufficientCoinsException());

        // When & Then
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber)
                        .param("couponType", couponType.name())
                        .param("coinCount", String.valueOf(coinCount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_COINS"));
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 API - 잘못된 파라미터")
    void enterLottery_InvalidParameters() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        CouponType couponType = CouponType.ONE_DAY;
        int invalidCoinCount = 0; // 0은 유효하지 않음

        // When & Then
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", phoneNumber)
                        .param("couponType", couponType.name())
                        .param("coinCount", String.valueOf(invalidCoinCount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 취소 API - 성공")
    void cancelEntry_Success() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        Long entryId = 1L;
        
        CancelResult result = CancelResult.builder()
                .entryId(entryId)
                .phoneNumber(phoneNumber)
                .couponType(CouponType.ONE_DAY)
                .returnedCoins(2)
                .totalCoins(2)
                .cancelTime(LocalDateTime.now())
                .build();
        ApiResponse<CancelResult> response = ApiResponse.success("휴가 쿠폰 응모가 취소되었습니다", result);
        
        given(vacationCouponService.cancelEntry(phoneNumber, entryId)).willReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/coupons/cancel")
                        .param("phoneNumber", phoneNumber)
                        .param("entryId", entryId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("휴가 쿠폰 응모가 취소되었습니다"))
                .andExpect(jsonPath("$.data.entryId").value(1))
                .andExpect(jsonPath("$.data.phoneNumber").value(phoneNumber))
                .andExpect(jsonPath("$.data.couponType").value("ONE_DAY"))
                .andExpect(jsonPath("$.data.returnedCoins").value(2));
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 취소 API - 응모 내역 없음")
    void cancelEntry_EntryNotFound() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        Long entryId = 999L;
        
        given(vacationCouponService.cancelEntry(phoneNumber, entryId))
                .willThrow(new EntryException.EntryNotFoundException());

        // When & Then
        mockMvc.perform(delete("/api/coupons/cancel")
                        .param("phoneNumber", phoneNumber)
                        .param("entryId", entryId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENTRY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("응모 내역을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("사용자 응모 현황 조회 API - 성공")
    void getUserEntries_Success() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        
        List<UserEntryStatus> entries = Arrays.asList(
                UserEntryStatus.builder()
                        .entryId(1L)
                        .couponType(CouponType.ONE_DAY)
                        .coinCount(2)
                        .isActive(true)
                        .entryTime(LocalDateTime.now())
                        .isWinner(false)
                        .build(),
                UserEntryStatus.builder()
                        .entryId(2L)
                        .couponType(CouponType.THREE_DAY)
                        .coinCount(1)
                        .isActive(true)
                        .entryTime(LocalDateTime.now())
                        .isWinner(false)
                        .build()
        );
        ApiResponse<List<UserEntryStatus>> response = ApiResponse.success(entries);
        
        given(vacationCouponService.getUserEntries(phoneNumber)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/coupons/my-entries")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].entryId").value(1))
                .andExpect(jsonPath("$.data[0].couponType").value("ONE_DAY"))
                .andExpect(jsonPath("$.data[0].coinCount").value(2))
                .andExpect(jsonPath("$.data[1].entryId").value(2))
                .andExpect(jsonPath("$.data[1].couponType").value("THREE_DAY"))
                .andExpect(jsonPath("$.data[1].coinCount").value(1));
    }

    @Test
    @DisplayName("전체 응모 현황 조회 API - 성공")
    void getAllEntries_Success() throws Exception {
        // Given
        List<CouponEntryStatus> entries = Arrays.asList(
                CouponEntryStatus.builder()
                        .couponType(CouponType.ONE_DAY)
                        .totalEntrants(25L)
                        .totalCoins(50L)
                        .averageCoins(2.0)
                        .isLotteryCompleted(false)
                        .winnerCount(0)
                        .build(),
                CouponEntryStatus.builder()
                        .couponType(CouponType.THREE_DAY)
                        .totalEntrants(20L)
                        .totalCoins(35L)
                        .averageCoins(1.75)
                        .isLotteryCompleted(false)
                        .winnerCount(0)
                        .build()
        );
        ApiResponse<List<CouponEntryStatus>> response = ApiResponse.success(entries);
        
        given(vacationCouponService.getAllEntries()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/coupons/all-entries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].couponType").value("ONE_DAY"))
                .andExpect(jsonPath("$.data[0].totalEntrants").value(25))
                .andExpect(jsonPath("$.data[0].totalCoins").value(50))
                .andExpect(jsonPath("$.data[1].couponType").value("THREE_DAY"))
                .andExpect(jsonPath("$.data[1].totalEntrants").value(20))
                .andExpect(jsonPath("$.data[1].totalCoins").value(35));
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 API - 필수 파라미터 누락")
    void enterLottery_MissingRequiredParameters() throws Exception {
        // When & Then - 쿠폰 타입 누락
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", "010-1234-5678")
                        .param("coinCount", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));

        // When & Then - 코인 수 누락
        mockMvc.perform(post("/api/coupons/enter")
                        .param("phoneNumber", "010-1234-5678")
                        .param("couponType", "ONE_DAY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
    }
}