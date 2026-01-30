package com.kidaristudio.vacationcouponlottery.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 기본 클래스
 * 모든 비즈니스 로직 관련 예외의 부모 클래스입니다.
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 오류 코드
     */
    private final String errorCode;
    
    /**
     * 오류 메시지
     */
    private final String errorMessage;

    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}