package com.kidaristudio.vacationcouponlottery.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 공통 응답 클래스
 * 모든 API 응답에서 일관된 형식을 제공합니다.
 * code, message, time, data 필드를 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * 응답 코드 (SUCCESS, ERROR 등)
     */
    private String code;
    
    /**
     * 응답 메시지 (한글)
     */
    private String message;
    
    /**
     * 응답 시간
     */
    private LocalDateTime time;
    
    /**
     * 응답 데이터
     */
    private T data;

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "성공", LocalDateTime.now(), data);
    }

    /**
     * 성공 응답 생성 (메시지 포함)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, LocalDateTime.now(), data);
    }

    /**
     * 오류 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, LocalDateTime.now(), null);
    }

    /**
     * 오류 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(code, message, LocalDateTime.now(), data);
    }
}