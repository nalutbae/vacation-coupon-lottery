package com.kidaristudio.vacationcouponlottery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 메시지 서비스
 * Spring의 MessageSource를 활용하여 다국어 메시지를 제공합니다.
 * 현재는 한글 메시지를 중심으로 구현되어 있습니다.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * 메시지 키로 메시지 조회
     * 
     * @param key 메시지 키
     * @return 현재 로케일에 맞는 메시지
     */
    public String getMessage(String key) {
        return getMessage(key, (Object[]) null);
    }

    /**
     * 메시지 키와 파라미터로 메시지 조회
     * 
     * @param key 메시지 키
     * @param args 메시지 파라미터
     * @return 현재 로케일에 맞는 메시지
     */
    public String getMessage(String key, Object[] args) {
        return getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * 메시지 키, 파라미터, 로케일로 메시지 조회
     * 
     * @param key 메시지 키
     * @param args 메시지 파라미터
     * @param locale 로케일
     * @return 지정된 로케일에 맞는 메시지
     */
    public String getMessage(String key, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            // 메시지를 찾을 수 없는 경우 키를 그대로 반환
            return key;
        }
    }

    /**
     * 기본 메시지와 함께 메시지 조회
     * 메시지 키를 찾을 수 없는 경우 기본 메시지를 반환합니다.
     * 
     * @param key 메시지 키
     * @param defaultMessage 기본 메시지
     * @return 메시지 또는 기본 메시지
     */
    public String getMessage(String key, String defaultMessage) {
        return getMessage(key, (Object[]) null, defaultMessage);
    }

    /**
     * 기본 메시지와 파라미터와 함께 메시지 조회
     * 
     * @param key 메시지 키
     * @param args 메시지 파라미터
     * @param defaultMessage 기본 메시지
     * @return 메시지 또는 기본 메시지
     */
    public String getMessage(String key, Object[] args, String defaultMessage) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    // 자주 사용되는 메시지들을 위한 편의 메서드들

    /**
     * 성공 메시지 조회
     */
    public String getSuccessMessage(String key) {
        return getMessage("success." + key);
    }

    /**
     * 오류 메시지 조회
     */
    public String getErrorMessage(String key) {
        return getMessage("error." + key);
    }

    /**
     * 일반 메시지 조회
     */
    public String getGeneralMessage(String key) {
        return getMessage("message." + key);
    }

    /**
     * 파라미터가 있는 성공 메시지 조회
     */
    public String getSuccessMessageWithArgs(String key, Object... args) {
        return getMessage("success." + key, args);
    }

    /**
     * 파라미터가 있는 오류 메시지 조회
     */
    public String getErrorMessageWithArgs(String key, Object... args) {
        return getMessage("error." + key, args);
    }

    /**
     * 파라미터가 있는 일반 메시지 조회
     */
    public String getGeneralMessageWithArgs(String key, Object... args) {
        return getMessage("message." + key, args);
    }
}