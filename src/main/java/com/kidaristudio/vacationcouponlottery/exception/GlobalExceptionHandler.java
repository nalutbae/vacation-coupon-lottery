package com.kidaristudio.vacationcouponlottery.exception;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 글로벌 예외 핸들러
 * 모든 예외를 일관된 형식으로 처리하고 국제화된 한글 오류 메시지를 제공합니다.
 * API 오류 추적을 위한 로깅도 포함합니다.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    /**
     * 응모 코인 관련 예외 처리
     */
    @ExceptionHandler({
            CoinException.InsufficientCoinsException.class,
            CoinException.CoinLimitExceededException.class,
            CoinException.NoRemainingCoinsException.class,
            CoinException.CoinAcquisitionFailedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleCoinException(
            BusinessException e, HttpServletRequest request) {
        
        logError(request, e, "응모 코인 관련 예외 발생");
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getErrorCode(), e.getErrorMessage()));
    }

    /**
     * 응모 관련 예외 처리
     */
    @ExceptionHandler({
            EntryException.UserNotFoundException.class,
            EntryException.EntryNotFoundException.class,
            EntryException.InvalidRequestException.class,
            EntryException.AlreadyCancelledException.class,
            EntryException.WinnerCannotCancelException.class,
            EntryException.EntryProcessingException.class,
            EntryException.NotLotteryPeriodException.class,
            EntryException.EntryPeriodEndedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleEntryException(
            BusinessException e, HttpServletRequest request) {
        
        logError(request, e, "응모 관련 예외 발생");
        
        HttpStatus status = determineHttpStatus(e);
        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getErrorCode(), e.getErrorMessage()));
    }

    /**
     * 추첨 관련 예외 처리
     */
    @ExceptionHandler({
            LotteryException.LotteryAlreadyCompletedException.class,
            LotteryException.NoEntrantsException.class,
            LotteryException.LotteryProcessingException.class,
            LotteryException.LotteryResultNotFoundException.class,
            LotteryException.InvalidLotteryRequestException.class,
            LotteryException.LotteryPermissionDeniedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleLotteryException(
            BusinessException e, HttpServletRequest request) {
        
        logError(request, e, "추첨 관련 예외 발생");
        
        HttpStatus status = determineHttpStatus(e);
        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getErrorCode(), e.getErrorMessage()));
    }

    /**
     * 권한 관련 예외 처리
     */
    @ExceptionHandler({
            EntryException.UnauthorizedAccessException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
            BusinessException e, HttpServletRequest request) {
        
        logError(request, e, "권한 관련 예외 발생");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getErrorCode(), e.getErrorMessage()));
    }

    /**
     * 메서드 인자 검증 실패 예외 처리 (Spring 6.1+)
     */
    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
            org.springframework.web.method.annotation.HandlerMethodValidationException e, HttpServletRequest request) {
        
        logError(request, e, "메서드 인자 검증 실패");
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_FAILED", messageService.getMessage("VALIDATION_FAILED")));
    }

    /**
     * 유효성 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        logError(request, e, "유효성 검증 실패");
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_FAILED", messageService.getMessage("VALIDATION_FAILED") + ": " + errorMessage));
    }

    /**
     * 바인딩 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(
            BindException e, HttpServletRequest request) {
        
        logError(request, e, "바인딩 예외 발생");
        
        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("BINDING_ERROR", messageService.getMessage("BINDING_ERROR") + ": " + errorMessage));
    }

    /**
     * 제약 조건 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        
        logError(request, e, "제약 조건 위반 예외 발생");
        
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("CONSTRAINT_VIOLATION", messageService.getMessage("CONSTRAINT_VIOLATION") + ": " + errorMessage));
    }

    /**
     * 요청 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        
        logError(request, e, "필수 파라미터 누락");
        
        String message = String.format("%s: %s", messageService.getMessage("MISSING_PARAMETER"), e.getParameterName());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("MISSING_PARAMETER", message));
    }

    /**
     * 메서드 인자 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        
        logError(request, e, "메서드 인자 타입 불일치");
        
        String message = String.format("%s: %s", messageService.getMessage("TYPE_MISMATCH"), e.getName());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("TYPE_MISMATCH", message));
    }

    /**
     * HTTP 메서드 지원하지 않음 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        
        logError(request, e, "지원하지 않는 HTTP 메서드");
        
        String message = String.format("%s: %s", messageService.getMessage("METHOD_NOT_ALLOWED"), e.getMethod());
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("METHOD_NOT_ALLOWED", message));
    }

    /**
     * 핸들러를 찾을 수 없음 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        
        logError(request, e, "핸들러를 찾을 수 없음");
        
        String message = String.format("%s: %s %s", messageService.getMessage("NOT_FOUND"), e.getHttpMethod(), e.getRequestURL());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", message));
    }

    /**
     * 리소스를 찾을 수 없음 예외 처리 (Spring Boot 3.x)
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException e, HttpServletRequest request) {
        
        logError(request, e, "리소스를 찾을 수 없음");
        
        String message = String.format("%s: %s", messageService.getMessage("NOT_FOUND"), e.getResourcePath());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", message));
    }

    /**
     * HTTP 메시지 읽기 불가 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        
        logError(request, e, "HTTP 메시지 읽기 불가");
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("MESSAGE_NOT_READABLE", messageService.getMessage("MESSAGE_NOT_READABLE")));
    }

    /**
     * 데이터베이스 접근 예외 처리
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(
            DataAccessException e, HttpServletRequest request) {
        
        logError(request, e, "데이터베이스 접근 예외 발생");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DATABASE_ERROR", messageService.getMessage("DATABASE_ERROR")));
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception e, HttpServletRequest request) {
        
        logError(request, e, "예상하지 못한 예외 발생");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", messageService.getMessage("INTERNAL_SERVER_ERROR")));
    }

    /**
     * HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatus(BusinessException e) {
        String errorCode = e.getErrorCode();
        
        if (errorCode.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        } else if (errorCode.contains("UNAUTHORIZED") || errorCode.contains("PERMISSION_DENIED")) {
            return HttpStatus.FORBIDDEN;
        } else if (errorCode.contains("ALREADY_") || errorCode.contains("INVALID_")) {
            return HttpStatus.CONFLICT;
        } else {
            return HttpStatus.BAD_REQUEST;
        }
    }

    /**
     * 오류 로깅
     */
    private void logError(HttpServletRequest request, Exception e, String description) {
        String requestInfo = String.format(
                "[%s] %s %s - User-Agent: %s, Remote-Addr: %s",
                LocalDateTime.now(),
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("User-Agent"),
                getClientIpAddress(request)
        );
        
        log.error("{} - {} - {}", description, requestInfo, e.getMessage(), e);
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}