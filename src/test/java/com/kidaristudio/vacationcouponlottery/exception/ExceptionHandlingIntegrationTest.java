package com.kidaristudio.vacationcouponlottery.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
import com.kidaristudio.vacationcouponlottery.service.VacationCouponService;
import com.kidaristudio.vacationcouponlottery.service.LotteryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 예외 처리 통합 테스트
 * 실제 컨트롤러를 통해 예외 처리 흐름을 검증합니다.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
class ExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntryCoinService entryCoinService;

    @MockBean
    private VacationCouponService vacationCouponService;

    @MockBean
    private LotteryService lotteryService;

    @Test
    @DisplayName("비즈니스 예외 통합 테스트 - 응모 코인 부족")
    void businessExceptionIntegration_InsufficientCoins() throws Exception {
        // Given
        when(entryCoinService.acquireCoin(anyString()))
                .thenThrow(new CoinException.InsufficientCoinsException());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "010-1234-5678")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        assertThat(apiResponse.getCode()).isEqualTo("INSUFFICIENT_COINS");
        assertThat(apiResponse.getMessage()).isEqualTo("응모 코인이 부족합니다.");
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getData()).isNull();
    }

    @Test
    @DisplayName("유효성 검증 실패 통합 테스트")
    void validationExceptionIntegration_InvalidPhoneNumber() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "") // 빈 전화번호
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        // 유효성 검증 실패 또는 파라미터 누락 오류가 발생해야 함
        assertThat(apiResponse.getCode()).isIn("VALIDATION_FAILED", "MISSING_PARAMETER", "CONSTRAINT_VIOLATION");
        assertThat(apiResponse.getMessage()).isNotNull();
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getData()).isNull();
    }

    @Test
    @DisplayName("HTTP 메서드 지원하지 않음 통합 테스트")
    void methodNotSupportedIntegration_DeleteMethod() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(delete("/api/coins/acquire")
                        .param("phoneNumber", "010-1234-5678")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        assertThat(apiResponse.getCode()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(apiResponse.getMessage()).contains("지원하지 않는 HTTP 메서드입니다");
        assertThat(apiResponse.getMessage()).contains("DELETE");
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getData()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트 통합 테스트")
    void notFoundIntegration_NonExistentEndpoint() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // 404 오류 페이지가 반환되거나 JSON 응답이 반환될 수 있음
        // 실제 설정에 따라 다를 수 있으므로 상태 코드만 검증
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("잘못된 JSON 형식 통합 테스트")
    void invalidJsonIntegration_MalformedJson() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/entries")
                        .content("{ invalid json }")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        assertThat(apiResponse.getCode()).isEqualTo("MESSAGE_NOT_READABLE");
        assertThat(apiResponse.getMessage()).isEqualTo("요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getData()).isNull();
    }

    @Test
    @DisplayName("다양한 비즈니스 예외 통합 테스트")
    void variousBusinessExceptionsIntegration() throws Exception {
        // 코인 한도 초과 예외
        when(entryCoinService.acquireCoin("010-1111-1111"))
                .thenThrow(new CoinException.CoinLimitExceededException());

        MvcResult result1 = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "010-1111-1111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent1 = result1.getResponse().getContentAsString();
        ApiResponse<?> apiResponse1 = objectMapper.readValue(responseContent1, ApiResponse.class);
        
        assertThat(apiResponse1.getCode()).isEqualTo("COIN_LIMIT_EXCEEDED");
        assertThat(apiResponse1.getMessage()).isEqualTo("응모 코인 한도를 초과했습니다. (최대 3개)");

        // 응모 코인 소진 예외
        when(entryCoinService.acquireCoin("010-2222-2222"))
                .thenThrow(new CoinException.NoRemainingCoinsException());

        MvcResult result2 = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "010-2222-2222")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent2 = result2.getResponse().getContentAsString();
        ApiResponse<?> apiResponse2 = objectMapper.readValue(responseContent2, ApiResponse.class);
        
        assertThat(apiResponse2.getCode()).isEqualTo("NO_REMAINING_COINS");
        assertThat(apiResponse2.getMessage()).isEqualTo("응모 코인이 모두 소진되었습니다.");
    }

    @Test
    @DisplayName("한글 오류 메시지 인코딩 통합 테스트")
    void koreanErrorMessageEncodingIntegration() throws Exception {
        // Given
        when(entryCoinService.acquireCoin(anyString()))
                .thenThrow(new CoinException.InsufficientCoinsException());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "010-1234-5678")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().encoding("UTF-8"))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        // 한글 메시지가 올바르게 인코딩되었는지 확인
        String message = apiResponse.getMessage();
        assertThat(message).isEqualTo("응모 코인이 부족합니다.");
        
        boolean containsKorean = message.chars()
                .anyMatch(ch -> Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.HANGUL_SYLLABLES);
        assertThat(containsKorean).isTrue();
    }

    @Test
    @DisplayName("오류 추적 정보 통합 테스트")
    void errorTrackingIntegration() throws Exception {
        // Given
        when(entryCoinService.acquireCoin(anyString()))
                .thenThrow(new CoinException.InsufficientCoinsException());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "010-1234-5678")
                        .header("User-Agent", "Test-Agent/1.0")
                        .header("X-Forwarded-For", "192.168.1.100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        // 추적 가능한 정보가 응답에 포함되어야 함
        assertThat(apiResponse.getCode()).isNotNull();
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getMessage()).isNotNull();
        
        // 로그에 추적 정보가 기록되었는지는 로그 레벨과 설정에 따라 다름
        // 여기서는 응답 형식의 일관성만 검증
    }

    @Test
    @DisplayName("API 응답 형식 일관성 통합 테스트")
    void apiResponseFormatConsistencyIntegration() throws Exception {
        // Given - 다양한 예외 상황 설정
        when(entryCoinService.acquireCoin("010-1111-1111"))
                .thenThrow(new CoinException.InsufficientCoinsException());
        when(entryCoinService.acquireCoin("010-2222-2222"))
                .thenThrow(new CoinException.CoinLimitExceededException());

        // When & Then - 첫 번째 예외
        MvcResult result1 = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "010-1111-1111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent1 = result1.getResponse().getContentAsString();
        ApiResponse<?> apiResponse1 = objectMapper.readValue(responseContent1, ApiResponse.class);

        // When & Then - 두 번째 예외
        MvcResult result2 = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", "010-2222-2222")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent2 = result2.getResponse().getContentAsString();
        ApiResponse<?> apiResponse2 = objectMapper.readValue(responseContent2, ApiResponse.class);

        // Then - 응답 형식 일관성 검증
        // 모든 응답이 동일한 구조를 가져야 함
        assertThat(apiResponse1.getCode()).isNotNull();
        assertThat(apiResponse1.getMessage()).isNotNull();
        assertThat(apiResponse1.getTime()).isNotNull();
        assertThat(apiResponse1.getData()).isNull();

        assertThat(apiResponse2.getCode()).isNotNull();
        assertThat(apiResponse2.getMessage()).isNotNull();
        assertThat(apiResponse2.getTime()).isNotNull();
        assertThat(apiResponse2.getData()).isNull();

        // 응답 구조는 동일하지만 내용은 다름
        assertThat(apiResponse1.getCode()).isNotEqualTo(apiResponse2.getCode());
        assertThat(apiResponse1.getMessage()).isNotEqualTo(apiResponse2.getMessage());
    }
}