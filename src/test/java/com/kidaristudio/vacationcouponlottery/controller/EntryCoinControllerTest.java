package com.kidaristudio.vacationcouponlottery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.exception.CoinException;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EntryCoinController 통합 테스트
 * 응모 코인 관리 API의 동작을 검증합니다.
 */
@WebMvcTest(EntryCoinController.class)
class EntryCoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntryCoinService entryCoinService;

    @Test
    @DisplayName("응모 코인 획득 API - 성공")
    void acquireCoin_Success() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        CoinAcquisitionResult result = CoinAcquisitionResult.builder()
                .phoneNumber(phoneNumber)
                .coinCount(1)
                .acquiredCoins(1)
                .remainingCoins(899)
                .build();
        ApiResponse<CoinAcquisitionResult> response = ApiResponse.success("응모 코인을 성공적으로 획득했습니다", result);
        
        given(entryCoinService.acquireCoin(phoneNumber)).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("응모 코인을 성공적으로 획득했습니다"))
                .andExpect(jsonPath("$.data.phoneNumber").value(phoneNumber))
                .andExpect(jsonPath("$.data.coinCount").value(1))
                .andExpect(jsonPath("$.data.acquiredCoins").value(1))
                .andExpect(jsonPath("$.data.remainingCoins").value(899));
    }

    @Test
    @DisplayName("응모 코인 획득 API - 한도 초과 예외")
    void acquireCoin_CoinLimitExceeded() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        
        given(entryCoinService.acquireCoin(phoneNumber))
                .willThrow(new CoinException.CoinLimitExceededException());

        // When & Then
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COIN_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("응모 코인 한도를 초과했습니다"));
    }

    @Test
    @DisplayName("응모 코인 획득 API - 코인 소진 예외")
    void acquireCoin_NoRemainingCoins() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        
        given(entryCoinService.acquireCoin(phoneNumber))
                .willThrow(new CoinException.NoRemainingCoinsException());

        // When & Then
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NO_REMAINING_COINS"))
                .andExpect(jsonPath("$.message").value("응모 코인이 모두 소진되었습니다"));
    }

    @Test
    @DisplayName("응모 코인 획득 API - 잘못된 전화번호 형식")
    void acquireCoin_InvalidPhoneNumberFormat() throws Exception {
        // Given
        String invalidPhoneNumber = "010-12345-678";

        // When & Then
        mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", invalidPhoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("응모 코인 획득 API - 전화번호 누락")
    void acquireCoin_MissingPhoneNumber() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/coins/acquire")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
    }

    @Test
    @DisplayName("사용자 코인 수량 조회 API - 성공")
    void getUserCoinCount_Success() throws Exception {
        // Given
        String phoneNumber = "010-1234-5678";
        ApiResponse<Integer> response = ApiResponse.success(2);
        
        given(entryCoinService.getUserCoinCount(phoneNumber)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/coins/count")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    @DisplayName("사용자 코인 수량 조회 API - 잘못된 전화번호 형식")
    void getUserCoinCount_InvalidPhoneNumberFormat() throws Exception {
        // Given
        String invalidPhoneNumber = "010-12345-678";

        // When & Then
        mockMvc.perform(get("/api/coins/count")
                        .param("phoneNumber", invalidPhoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("전체 코인 현황 조회 API - 성공")
    void getAllCoinStatus_Success() throws Exception {
        // Given
        CoinStatusResponse statusResponse = CoinStatusResponse.builder()
                .totalCoins(900)
                .remainingCoins(850)
                .distributedCoins(50)
                .usersWithCoins(25L)
                .usersWithMaxCoins(5L)
                .build();
        ApiResponse<CoinStatusResponse> response = ApiResponse.success(statusResponse);
        
        given(entryCoinService.getAllCoinStatus()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/coins/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalCoins").value(900))
                .andExpect(jsonPath("$.data.remainingCoins").value(850))
                .andExpect(jsonPath("$.data.distributedCoins").value(50))
                .andExpect(jsonPath("$.data.usersWithCoins").value(25));
    }
}