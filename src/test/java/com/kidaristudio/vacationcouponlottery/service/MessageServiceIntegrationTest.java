package com.kidaristudio.vacationcouponlottery.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MessageService 통합 테스트
 * 실제 messages.properties 파일과 연동하여 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MessageService 통합 테스트")
class MessageServiceIntegrationTest {

    @Autowired
    private MessageService messageService;

    @Test
    @DisplayName("실제 메시지 파일에서 코인 관련 메시지를 정상적으로 조회한다")
    void getMessage_CoinMessages_ReturnsCorrectMessages() {
        // when & then
        assertThat(messageService.getMessage("INSUFFICIENT_COINS"))
                .isEqualTo("응모 코인이 부족합니다.");
        
        assertThat(messageService.getMessage("COIN_LIMIT_EXCEEDED"))
                .isEqualTo("응모 코인 한도를 초과했습니다. (최대 3개)");
        
        assertThat(messageService.getMessage("NO_REMAINING_COINS"))
                .isEqualTo("응모 코인이 모두 소진되었습니다.");
        
        assertThat(messageService.getMessage("COIN_ACQUISITION_FAILED"))
                .isEqualTo("응모 코인 획득에 실패했습니다.");
    }

    @Test
    @DisplayName("실제 메시지 파일에서 응모 관련 메시지를 정상적으로 조회한다")
    void getMessage_EntryMessages_ReturnsCorrectMessages() {
        // when & then
        assertThat(messageService.getMessage("ENTRY_NOT_FOUND"))
                .isEqualTo("응모 내역을 찾을 수 없습니다.");
        
        assertThat(messageService.getMessage("ALREADY_CANCELLED"))
                .isEqualTo("이미 취소된 응모입니다.");
        
        assertThat(messageService.getMessage("WINNER_CANNOT_CANCEL"))
                .isEqualTo("당첨된 응모는 취소할 수 없습니다.");
        
        assertThat(messageService.getMessage("USER_NOT_FOUND"))
                .isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("실제 메시지 파일에서 추첨 관련 메시지를 정상적으로 조회한다")
    void getMessage_LotteryMessages_ReturnsCorrectMessages() {
        // when & then
        assertThat(messageService.getMessage("LOTTERY_ALREADY_COMPLETED"))
                .isEqualTo("이미 추첨이 완료되었습니다.");
        
        assertThat(messageService.getMessage("NO_ENTRANTS"))
                .isEqualTo("응모자가 없어 추첨을 진행할 수 없습니다.");
        
        assertThat(messageService.getMessage("LOTTERY_PERMISSION_DENIED"))
                .isEqualTo("추첨을 실행할 권한이 없습니다.");
    }

    @Test
    @DisplayName("실제 메시지 파일에서 시스템 관련 메시지를 정상적으로 조회한다")
    void getMessage_SystemMessages_ReturnsCorrectMessages() {
        // when & then
        assertThat(messageService.getMessage("VALIDATION_FAILED"))
                .isEqualTo("입력값 검증에 실패했습니다.");
        
        assertThat(messageService.getMessage("INTERNAL_SERVER_ERROR"))
                .isEqualTo("서버 내부 오류가 발생했습니다.");
        
        assertThat(messageService.getMessage("DATABASE_ERROR"))
                .isEqualTo("데이터베이스 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 메시지 키의 경우 키를 그대로 반환한다")
    void getMessage_NonExistentKey_ReturnsKey() {
        // given
        String nonExistentKey = "NON_EXISTENT_KEY";

        // when
        String result = messageService.getMessage(nonExistentKey);

        // then
        assertThat(result).isEqualTo(nonExistentKey);
    }

    @Test
    @DisplayName("기본 메시지가 있는 경우 정상적으로 처리한다")
    void getMessage_WithDefaultMessage_WorksCorrectly() {
        // given
        String existingKey = "INSUFFICIENT_COINS";
        String nonExistentKey = "NON_EXISTENT_KEY";
        String defaultMessage = "기본 메시지";

        // when & then
        assertThat(messageService.getMessage(existingKey, defaultMessage))
                .isEqualTo("응모 코인이 부족합니다.");
        
        assertThat(messageService.getMessage(nonExistentKey, defaultMessage))
                .isEqualTo(defaultMessage);
    }
}