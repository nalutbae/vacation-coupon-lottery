package com.kidaristudio.vacationcouponlottery.service.impl;

import com.kidaristudio.vacationcouponlottery.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 메시지 국제화 서비스 구현체
 * Spring MessageSource를 활용하여 메시지 해결 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageSource messageSource;
    
    /**
     * 기본 로케일 (한국어)
     */
    private static final Locale DEFAULT_LOCALE = Locale.KOREAN;
    
    @Override
    public String getMessage(String key) {
        try {
            return messageSource.getMessage(key, null, DEFAULT_LOCALE);
        } catch (NoSuchMessageException e) {
            log.warn("메시지 키를 찾을 수 없습니다: {}", key);
            return key; // 키를 그대로 반환
        }
    }
    
    @Override
    public String getMessage(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, DEFAULT_LOCALE);
        } catch (NoSuchMessageException e) {
            log.warn("메시지 키를 찾을 수 없습니다: {}", key);
            return key; // 키를 그대로 반환
        }
    }
    
    @Override
    public String getMessage(String key, String defaultMessage) {
        try {
            return messageSource.getMessage(key, null, DEFAULT_LOCALE);
        } catch (NoSuchMessageException e) {
            log.debug("메시지 키를 찾을 수 없어 기본 메시지를 사용합니다. 키: {}, 기본 메시지: {}", key, defaultMessage);
            return defaultMessage;
        }
    }
    
    @Override
    public String getMessage(String key, String defaultMessage, Object... args) {
        try {
            return messageSource.getMessage(key, args, DEFAULT_LOCALE);
        } catch (NoSuchMessageException e) {
            log.debug("메시지 키를 찾을 수 없어 기본 메시지를 사용합니다. 키: {}, 기본 메시지: {}", key, defaultMessage);
            return String.format(defaultMessage, args);
        }
    }
}