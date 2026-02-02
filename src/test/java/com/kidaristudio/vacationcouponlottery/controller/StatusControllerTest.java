package com.kidaristudio.vacationcouponlottery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.*;
import com.kidaristudio.vacationcouponlottery.service.MessageService;
import com.kidaristudio.vacationcouponlottery.service.StatusService;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StatusController 통합 테스트
 * 현황 조회 API의 동작을 검증합니다.
 */
@WebMvcTest(StatusController.class)
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatusService statusService;

    @MockBean
    private MessageService messageService;

    @Test
    @DisplayName("사용자 개인 응모 현황 조회 API - 성공")
    void getUserEntryStatus_Success() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        
        List<UserEntryStatus> entries = Arrays.asList(
                UserEntryStatus.builder()
                        .entryId(1L)
                        .couponType(CouponType.ONE_DAY)
                        .coinCount(2)
                        .isActive(true)
                        .entryTime(LocalDateTime.now())
                        .build(),
                UserEntryStatus.builder()
                        .entryId(2L)
                        .couponType(CouponType.THREE_DAY)
                        .coinCount(1)
                        .isActive(true)
                        .entryTime(LocalDateTime.now())
                        .build()
        );
        ApiResponse<List<UserEntryStatus>> response = ApiResponse.success(entries);
        
        given(statusService.getUserEntryStatus(phoneNumber)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/status/user/entries")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].entryId").value(1))
                .andExpect(jsonPath("$.data[0].couponType").value("ONE_DAY"))
                .andExpect(jsonPath("$.data[0].coinCount").value(2))
                .andExpect(jsonPath("$.data[0].isActive").value(true))
                .andExpect(jsonPath("$.data[1].entryId").value(2))
                .andExpect(jsonPath("$.data[1].couponType").value("THREE_DAY"))
                .andExpect(jsonPath("$.data[1].coinCount").value(1))
                .andExpect(jsonPath("$.data[1].isActive").value(true));
    }

    @Test
    @DisplayName("사용자 개인 응모 현황 조회 API - 잘못된 전화번호 형식")
    void getUserEntryStatus_InvalidPhoneNumberFormat() throws Exception {
        // Given
        String invalidPhoneNumber = "010-12345-678";

        // When & Then
        mockMvc.perform(get("/api/status/user/entries")
                        .param("phoneNumber", invalidPhoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("사용자 응모 코인 수량 조회 API - 성공")
    void getUserCoinCount_Success() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        ApiResponse<Integer> response = ApiResponse.success(2);
        
        given(statusService.getUserCoinCount(phoneNumber)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/status/user/coins")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    @DisplayName("전체 응모 현황 조회 API - 성공")
    void getAllEntryStatus_Success() throws Exception {
        // Given
        List<CouponEntryStatus> entries = Arrays.asList(
                CouponEntryStatus.builder()
                        .couponType(CouponType.ONE_DAY)
                        .totalEntrants(25L)
                        .totalCoins(50L)
                        .build(),
                CouponEntryStatus.builder()
                        .couponType(CouponType.THREE_DAY)
                        .totalEntrants(20L)
                        .totalCoins(35L)
                        .build()
        );
        ApiResponse<List<CouponEntryStatus>> response = ApiResponse.success(entries);
        
        given(statusService.getAllEntryStatus()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/status/entries")
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
    @DisplayName("전체 응모 코인 현황 조회 API - 성공")
    void getAllCoinStatus_Success() throws Exception {
        // Given
        CoinStatusResponse statusResponse = CoinStatusResponse.builder()
                .totalCoins(900)
                .remainingCoins(850)
                .distributedCoins(50)
                .usersWithCoins(25L)
                .build();
        ApiResponse<CoinStatusResponse> response = ApiResponse.success(statusResponse);
        
        given(statusService.getAllCoinStatus()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/status/coins")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.remainingCoins").value(850))
                .andExpect(jsonPath("$.data.distributedCoins").value(50))
                .andExpect(jsonPath("$.data.usersWithCoins").value(25));
    }

    @Test
    @DisplayName("시스템 전체 통계 조회 API - 성공")
    void getSystemStatistics_Success() throws Exception {
        // Given
        StatusService.SystemStatistics statistics = new StatusService.SystemStatistics(
                100,    // totalUsers
                150,    // totalEntries
                75,     // totalCoinsDistributed
                825,    // remainingCoins
                false   // isLotteryCompleted
        );
        ApiResponse<StatusService.SystemStatistics> response = ApiResponse.success(statistics);
        
        given(statusService.getSystemStatistics()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/status/system")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalEntries").value(150))
                .andExpect(jsonPath("$.data.totalCoinsDistributed").value(75))
                .andExpect(jsonPath("$.data.remainingCoins").value(825))
                .andExpect(jsonPath("$.data.isLotteryCompleted").value(false));
    }

    @Test
    @DisplayName("사용자 응모 코인 수량 조회 API - 전화번호 누락")
    void getUserCoinCount_MissingPhoneNumber() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/status/user/coins")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
    }

    @Test
    @DisplayName("사용자 개인 응모 현황 조회 API - 빈 전화번호")
    void getUserEntryStatus_BlankPhoneNumber() throws Exception {
        // Given
        String blankPhoneNumber = "";

        // When & Then
        mockMvc.perform(get("/api/status/user/entries")
                        .param("phoneNumber", blankPhoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}