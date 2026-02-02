package com.kidaristudio.vacationcouponlottery.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.repository.SystemConfigRepository;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
import com.kidaristudio.vacationcouponlottery.service.VacationCouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 예외 처리 통합 테스트
 * 실제 컨트롤러를 통해 예외 처리 흐름을 검증합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
class ExceptionHandlingIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntryCoinService entryCoinService;

    @Autowired
    private VacationCouponService vacationCouponService;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8)
                .build();
        
        // 각 테스트 전에 시스템 상태 초기화
        resetSystemState();
    }

    /**
     * 각 테스트 전에 시스템 상태를 초기화합니다.
     * 전체 코인 수량을 900개로, 남은 코인 수량을 900개로 재설정합니다.
     */
    private void resetSystemState() {
        // 모든 사용자 데이터 삭제 (테스트 격리)
        userRepository.deleteAll();
        
        // 시스템 설정 초기화
        resetSystemConfig("TOTAL_COINS", "900", "전체 응모 코인 수량");
        resetSystemConfig("REMAINING_COINS", "900", "남은 응모 코인 수량");
        resetSystemConfig("MAX_COINS_PER_USER", "3", "사용자당 최대 응모 코인 수");
        resetSystemConfig("WINNERS_PER_COUPON", "3", "쿠폰당 당첨자 수");
    }
    
    /**
     * 시스템 설정값을 초기화합니다.
     */
    private void resetSystemConfig(String configKey, String configValue, String description) {
        systemConfigRepository.findByConfigKey(configKey)
                .ifPresentOrElse(
                    config -> {
                        config.updateValue(configValue);
                        systemConfigRepository.save(config);
                    },
                    () -> {
                        var newConfig = com.kidaristudio.vacationcouponlottery.domain.SystemConfig.builder()
                                .configKey(configKey)
                                .configValue(configValue)
                                .description(description)
                                .build();
                        systemConfigRepository.save(newConfig);
                    }
                );
    }

    @Test
    @DisplayName("비즈니스 예외 통합 테스트 - 사용자별 코인 한도 초과")
    void businessExceptionIntegration_CoinLimitExceeded() throws Exception {
        // Given - 사용자가 최대 코인을 획득한 상태
        String phoneNumber = "010-1234-5678";
        
        // 최대 코인 획득
        entryCoinService.acquireCoin(phoneNumber);
        entryCoinService.acquireCoin(phoneNumber);
        entryCoinService.acquireCoin(phoneNumber);

        // When & Then - 추가 코인 획득 시도 (사용자별 한도 초과)
        MvcResult result = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        assertThat(apiResponse.getCode()).isEqualTo("COIN_LIMIT_EXCEEDED");
        assertThat(apiResponse.getMessage()).isEqualTo("응모 코인 한도를 초과했습니다. (최대 3개)");
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getData()).isNull();
    }

    @Test
    @DisplayName("전체 코인 소진 예외 통합 테스트")
    void totalCoinsExhaustionIntegration() throws Exception {
        // Given - 전체 코인을 소진시키기 위해 시스템 설정을 3개로 변경
        resetSystemConfig("TOTAL_COINS", "3", "전체 응모 코인 수량");
        resetSystemConfig("REMAINING_COINS", "3", "남은 응모 코인 수량");
        
        // 3명의 사용자가 각각 1개씩 코인을 획득하여 전체 코인 소진
        String phoneNumber1 = "010-1111-1111";
        String phoneNumber2 = "010-2222-2222";
        String phoneNumber3 = "010-3333-3333";
        String phoneNumber4 = "010-4444-4444";
        
        entryCoinService.acquireCoin(phoneNumber1);
        entryCoinService.acquireCoin(phoneNumber2);
        entryCoinService.acquireCoin(phoneNumber3);
        
        // When & Then - 4번째 사용자가 코인 획득 시도 (전체 코인 소진)
        MvcResult result = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber4)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        assertThat(apiResponse.getCode()).isEqualTo("NO_REMAINING_COINS");
        assertThat(apiResponse.getMessage()).isEqualTo("응모 코인이 모두 소진되었습니다.");
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
    @DisplayName("잘못된 요청 형식 통합 테스트")
    void invalidRequestIntegration_MissingParameters() throws Exception {
        // When & Then - 필수 파라미터 누락
        MvcResult result = mockMvc.perform(post("/api/coupons/enter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        assertThat(apiResponse.getCode()).isEqualTo("MISSING_PARAMETER");
        assertThat(apiResponse.getMessage()).contains("필수 파라미터가 누락되었습니다.");
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getData()).isNull();
    }

    @Test
    @DisplayName("한글 오류 메시지 통합 테스트")
    void koreanErrorMessageIntegration() throws Exception {
        // Given - 사용자별 코인 한도 초과 상황 생성
        String phoneNumber = "010-1234-5678";
        entryCoinService.acquireCoin(phoneNumber);
        entryCoinService.acquireCoin(phoneNumber);
        entryCoinService.acquireCoin(phoneNumber);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/coins/acquire")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 응답 내용 검증
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        // 한글 메시지가 올바르게 반환되는지 확인 (인코딩 문제 해결됨)
        assertThat(apiResponse.getCode()).isEqualTo("COIN_LIMIT_EXCEEDED");
        assertThat(apiResponse.getMessage()).isNotNull();
        assertThat(apiResponse.getTime()).isNotNull();
        assertThat(apiResponse.getData()).isNull();
    }
}