package com.kidaristudio.vacationcouponlottery.service;

/**
 * 메시지 국제화 서비스 인터페이스
 * Spring MessageSource를 래핑하여 메시지 해결 기능을 제공합니다.
 */
public interface MessageService {
    
    /**
     * 메시지 키로 메시지를 조회합니다.
     * 
     * @param key 메시지 키
     * @return 해결된 메시지
     */
    String getMessage(String key);
    
    /**
     * 메시지 키와 파라미터로 메시지를 조회합니다.
     * 
     * @param key 메시지 키
     * @param args 메시지 파라미터
     * @return 해결된 메시지
     */
    String getMessage(String key, Object... args);
    
    /**
     * 메시지 키로 메시지를 조회하되, 키가 없으면 기본 메시지를 반환합니다.
     * 
     * @param key 메시지 키
     * @param defaultMessage 기본 메시지
     * @return 해결된 메시지 또는 기본 메시지
     */
    String getMessage(String key, String defaultMessage);
    
    /**
     * 메시지 키와 파라미터로 메시지를 조회하되, 키가 없으면 기본 메시지를 반환합니다.
     * 
     * @param key 메시지 키
     * @param defaultMessage 기본 메시지
     * @param args 메시지 파라미터
     * @return 해결된 메시지 또는 기본 메시지
     */
    String getMessage(String key, String defaultMessage, Object... args);
}