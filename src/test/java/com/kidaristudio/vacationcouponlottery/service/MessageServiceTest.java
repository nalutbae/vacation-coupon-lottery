package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * MessageService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 테스트")
class MessageServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    @DisplayName("메시지 키로 메시지를 정상적으로 조회한다")
    void getMessage_WithValidKey_ReturnsMessage() {
        // given
        String key = "INSUFFICIENT_COINS";
        String expectedMessage = "응모 코인이 부족합니다.";
        when(messageSource.getMessage(eq(key), eq(null), eq(Locale.KOREAN)))
                .thenReturn(expectedMessage);

        // when
        String actualMessage = messageService.getMessage(key);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 키의 경우 키를 그대로 반환한다")
    void getMessage_WithInvalidKey_ReturnsKey() {
        // given
        String key = "INVALID_KEY";
        when(messageSource.getMessage(eq(key), eq(null), eq(Locale.KOREAN)))
                .thenThrow(new NoSuchMessageException(key));

        // when
        String actualMessage = messageService.getMessage(key);

        // then
        assertThat(actualMessage).isEqualTo(key);
    }

    @Test
    @DisplayName("파라미터가 있는 메시지를 정상적으로 조회한다")
    void getMessage_WithParameters_ReturnsFormattedMessage() {
        // given
        String key = "LOTTERY_ALREADY_COMPLETED";
        Object[] args = {"휴가"};
        String expectedMessage = "휴가 쿠폰에 대한 추첨이 이미 완료되었습니다.";
        when(messageSource.getMessage(eq(key), eq(args), eq(Locale.KOREAN)))
                .thenReturn(expectedMessage);

        // when
        String actualMessage = messageService.getMessage(key, args);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("기본 메시지가 있는 경우 메시지를 정상적으로 조회한다")
    void getMessage_WithDefaultMessage_ReturnsMessage() {
        // given
        String key = "INSUFFICIENT_COINS";
        String defaultMessage = "기본 메시지";
        String expectedMessage = "응모 코인이 부족합니다.";
        when(messageSource.getMessage(eq(key), eq(null), eq(Locale.KOREAN)))
                .thenReturn(expectedMessage);

        // when
        String actualMessage = messageService.getMessage(key, defaultMessage);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("메시지 키가 없고 기본 메시지가 있는 경우 기본 메시지를 반환한다")
    void getMessage_WithInvalidKeyAndDefaultMessage_ReturnsDefaultMessage() {
        // given
        String key = "INVALID_KEY";
        String defaultMessage = "기본 메시지";
        when(messageSource.getMessage(eq(key), eq(null), eq(Locale.KOREAN)))
                .thenThrow(new NoSuchMessageException(key));

        // when
        String actualMessage = messageService.getMessage(key, defaultMessage);

        // then
        assertThat(actualMessage).isEqualTo(defaultMessage);
    }

    @Test
    @DisplayName("파라미터와 기본 메시지가 있는 경우 메시지를 정상적으로 조회한다")
    void getMessage_WithParametersAndDefaultMessage_ReturnsFormattedMessage() {
        // given
        String key = "LOTTERY_ALREADY_COMPLETED";
        String defaultMessage = "%s 기본 메시지";
        Object[] args = {"휴가"};
        String expectedMessage = "휴가 쿠폰에 대한 추첨이 이미 완료되었습니다.";
        when(messageSource.getMessage(eq(key), eq(args), eq(Locale.KOREAN)))
                .thenReturn(expectedMessage);

        // when
        String actualMessage = messageService.getMessage(key, defaultMessage, args);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("메시지 키가 없고 파라미터와 기본 메시지가 있는 경우 포맷된 기본 메시지를 반환한다")
    void getMessage_WithInvalidKeyParametersAndDefaultMessage_ReturnsFormattedDefaultMessage() {
        // given
        String key = "INVALID_KEY";
        String defaultMessage = "%s 기본 메시지";
        Object[] args = {"테스트"};
        String expectedMessage = "테스트 기본 메시지";
        when(messageSource.getMessage(eq(key), eq(args), eq(Locale.KOREAN)))
                .thenThrow(new NoSuchMessageException(key));

        // when
        String actualMessage = messageService.getMessage(key, defaultMessage, args);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
}