package com.kidaristudio.vacationcouponlottery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MessageService 단위 테스트
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.messages.basename=messages",
    "spring.messages.encoding=UTF-8"
})
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        // 기본 로케일을 한국어로 설정
        LocaleContextHolder.setLocale(Locale.KOREA);
    }

    @Test
    @DisplayName("메시지 키로 메시지 조회 - 성공")
    void getMessage_WithKey_Success() {
        // When
        String message = messageService.getMessage("error.insufficient.coins");
        
        // Then
        assertThat(message).isEqualTo("응모 코인이 부족합니다.");
    }

    @Test
    @DisplayName("메시지 키와 파라미터로 메시지 조회 - 성공")
    void getMessage_WithKeyAndArgs_Success() {
        // Given
        Object[] args = {"phoneNumber"};
        
        // When
        String message = messageService.getMessage("error.missing.parameter", args);
        
        // Then
        assertThat(message).isEqualTo("필수 파라미터가 누락되었습니다.");
    }

    @Test
    @DisplayName("메시지 키, 파라미터, 로케일로 메시지 조회 - 성공")
    void getMessage_WithKeyArgsAndLocale_Success() {
        // Given
        Object[] args = null;
        Locale locale = Locale.KOREA;
        
        // When
        String message = messageService.getMessage("success.coin.acquired", args, locale);
        
        // Then
        assertThat(message).isEqualTo("응모 코인을 성공적으로 획득했습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 메시지 키 조회 - 키 반환")
    void getMessage_NonExistentKey_ReturnsKey() {
        // Given
        String nonExistentKey = "non.existent.key";
        
        // When
        String message = messageService.getMessage(nonExistentKey);
        
        // Then
        assertThat(message).isEqualTo(nonExistentKey);
    }

    @Test
    @DisplayName("기본 메시지와 함께 메시지 조회 - 성공")
    void getMessage_WithDefaultMessage_Success() {
        // Given
        String defaultMessage = "기본 메시지";
        
        // When
        String message = messageService.getMessage("error.coin.limit.exceeded", defaultMessage);
        
        // Then
        assertThat(message).isEqualTo("응모 코인 한도를 초과했습니다. (최대 3개)");
    }

    @Test
    @DisplayName("기본 메시지와 함께 메시지 조회 - 키 없음")
    void getMessage_WithDefaultMessage_KeyNotFound() {
        // Given
        String nonExistentKey = "non.existent.key";
        String defaultMessage = "기본 메시지";
        
        // When
        String message = messageService.getMessage(nonExistentKey, defaultMessage);
        
        // Then
        assertThat(message).isEqualTo(defaultMessage);
    }

    @Test
    @DisplayName("기본 메시지와 파라미터와 함께 메시지 조회 - 성공")
    void getMessage_WithDefaultMessageAndArgs_Success() {
        // Given
        Object[] args = null;
        String defaultMessage = "기본 메시지";
        
        // When
        String message = messageService.getMessage("success.entry.registered", args, defaultMessage);
        
        // Then
        assertThat(message).isEqualTo("휴가 쿠폰 응모가 완료되었습니다.");
    }

    @Test
    @DisplayName("성공 메시지 조회 - 성공")
    void getSuccessMessage_Success() {
        // When
        String message = messageService.getSuccessMessage("coin.acquired");
        
        // Then
        assertThat(message).isEqualTo("응모 코인을 성공적으로 획득했습니다.");
    }

    @Test
    @DisplayName("오류 메시지 조회 - 성공")
    void getErrorMessage_Success() {
        // When
        String message = messageService.getErrorMessage("insufficient.coins");
        
        // Then
        assertThat(message).isEqualTo("응모 코인이 부족합니다.");
    }

    @Test
    @DisplayName("일반 메시지 조회 - 성공")
    void getGeneralMessage_Success() {
        // When
        String message = messageService.getGeneralMessage("processing");
        
        // Then
        assertThat(message).isEqualTo("처리 중입니다...");
    }

    @Test
    @DisplayName("파라미터가 있는 성공 메시지 조회 - 성공")
    void getSuccessMessageWithArgs_Success() {
        // Given
        Object[] args = {"테스트"};
        
        // When
        String message = messageService.getSuccessMessageWithArgs("data.retrieved", args);
        
        // Then
        assertThat(message).isEqualTo("데이터 조회가 완료되었습니다.");
    }

    @Test
    @DisplayName("파라미터가 있는 오류 메시지 조회 - 성공")
    void getErrorMessageWithArgs_Success() {
        // Given
        Object[] args = {"테스트"};
        
        // When
        String message = messageService.getErrorMessageWithArgs("validation.failed", args);
        
        // Then
        assertThat(message).isEqualTo("입력값 검증에 실패했습니다.");
    }

    @Test
    @DisplayName("파라미터가 있는 일반 메시지 조회 - 성공")
    void getGeneralMessageWithArgs_Success() {
        // Given
        Object[] args = {"테스트"};
        
        // When
        String message = messageService.getGeneralMessageWithArgs("success", args);
        
        // Then
        assertThat(message).isEqualTo("성공");
    }

    @Test
    @DisplayName("존재하지 않는 성공 메시지 키 조회")
    void getSuccessMessage_NonExistentKey() {
        // When
        String message = messageService.getSuccessMessage("non.existent");
        
        // Then
        assertThat(message).isEqualTo("success.non.existent");
    }

    @Test
    @DisplayName("존재하지 않는 오류 메시지 키 조회")
    void getErrorMessage_NonExistentKey() {
        // When
        String message = messageService.getErrorMessage("non.existent");
        
        // Then
        assertThat(message).isEqualTo("error.non.existent");
    }

    @Test
    @DisplayName("존재하지 않는 일반 메시지 키 조회")
    void getGeneralMessage_NonExistentKey() {
        // When
        String message = messageService.getGeneralMessage("non.existent");
        
        // Then
        assertThat(message).isEqualTo("message.non.existent");
    }

    @Test
    @DisplayName("다양한 오류 메시지 조회 테스트")
    void getVariousErrorMessages_Success() {
        // 응모 코인 관련 오류 메시지
        assertThat(messageService.getErrorMessage("coin.limit.exceeded"))
                .isEqualTo("응모 코인 한도를 초과했습니다. (최대 3개)");
        assertThat(messageService.getErrorMessage("no.remaining.coins"))
                .isEqualTo("응모 코인이 모두 소진되었습니다.");
        
        // 응모 관련 오류 메시지
        assertThat(messageService.getErrorMessage("entry.not.found"))
                .isEqualTo("응모 내역을 찾을 수 없습니다.");
        assertThat(messageService.getErrorMessage("entry.already.cancelled"))
                .isEqualTo("이미 취소된 응모입니다.");
        
        // 추첨 관련 오류 메시지
        assertThat(messageService.getErrorMessage("lottery.already.completed"))
                .isEqualTo("이미 추첨이 완료되었습니다.");
        assertThat(messageService.getErrorMessage("lottery.no.entries"))
                .isEqualTo("응모자가 없어 추첨을 진행할 수 없습니다.");
    }

    @Test
    @DisplayName("다양한 성공 메시지 조회 테스트")
    void getVariousSuccessMessages_Success() {
        assertThat(messageService.getSuccessMessage("entry.registered"))
                .isEqualTo("휴가 쿠폰 응모가 완료되었습니다.");
        assertThat(messageService.getSuccessMessage("entry.cancelled"))
                .isEqualTo("휴가 쿠폰 응모가 취소되었습니다.");
        assertThat(messageService.getSuccessMessage("lottery.completed"))
                .isEqualTo("추첨이 완료되었습니다.");
    }

    @Test
    @DisplayName("한글 메시지 인코딩 테스트")
    void koreanMessageEncoding_Success() {
        // Given & When
        String message = messageService.getErrorMessage("user.not.found");
        
        // Then
        assertThat(message).isEqualTo("사용자를 찾을 수 없습니다.");
        
        // 한글 문자가 올바르게 인코딩되었는지 확인
        boolean containsKorean = message.chars()
                .anyMatch(ch -> Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.HANGUL_SYLLABLES);
        assertThat(containsKorean).isTrue();
    }
}