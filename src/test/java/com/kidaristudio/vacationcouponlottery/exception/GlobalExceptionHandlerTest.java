package com.kidaristudio.vacationcouponlottery.exception;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GlobalExceptionHandler 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;
    
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.addHeader("User-Agent", "Test-Agent");
        request.setRemoteAddr("127.0.0.1");
    }

    @Test
    @DisplayName("응모 코인 예외 처리 - 응모 코인 부족")
    void handleCoinException_InsufficientCoins() {
        // Given
        CoinException.InsufficientCoinsException exception = new CoinException.InsufficientCoinsException();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleCoinException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INSUFFICIENT_COINS");
        assertThat(response.getBody().getMessage()).isEqualTo("응모 코인이 부족합니다.");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("응모 코인 예외 처리 - 코인 한도 초과")
    void handleCoinException_CoinLimitExceeded() {
        // Given
        CoinException.CoinLimitExceededException exception = new CoinException.CoinLimitExceededException();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleCoinException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("COIN_LIMIT_EXCEEDED");
        assertThat(response.getBody().getMessage()).isEqualTo("응모 코인 한도를 초과했습니다. (최대 3개)");
    }

    @Test
    @DisplayName("응모 예외 처리 - 사용자 없음")
    void handleEntryException_UserNotFound() {
        // Given
        EntryException.UserNotFoundException exception = new EntryException.UserNotFoundException();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleEntryException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("추첨 예외 처리 - 이미 완료됨")
    void handleLotteryException_AlreadyCompleted() {
        // Given
        LotteryException.LotteryAlreadyCompletedException exception = new LotteryException.LotteryAlreadyCompletedException("1일권");
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleLotteryException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("LOTTERY_ALREADY_COMPLETED");
        assertThat(response.getBody().getMessage()).contains("1일권 쿠폰에 대한 추첨이 이미 완료되었습니다");
    }

    @Test
    @DisplayName("권한 예외 처리 - 권한 거부")
    void handleUnauthorizedException_PermissionDenied() {
        // Given
        EntryException.UnauthorizedAccessException exception = new EntryException.UnauthorizedAccessException();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleUnauthorizedException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode()).isEqualTo("UNAUTHORIZED_ACCESS");
        assertThat(response.getBody().getMessage()).isEqualTo("권한이 없습니다.");
    }

    @Test
    @DisplayName("바인딩 예외 처리")
    void handleBindException_Success() {
        // Given
        when(messageService.getMessage("BINDING_ERROR")).thenReturn("요청 데이터 바인딩에 실패했습니다.");
        
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "coinCount", "코인 수량은 숫자여야 합니다."));
        
        BindException exception = new BindException(bindingResult);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBindException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("BINDING_ERROR");
        assertThat(response.getBody().getMessage()).contains("요청 데이터 바인딩에 실패했습니다.");
        assertThat(response.getBody().getMessage()).contains("코인 수량은 숫자여야 합니다");
    }

    @Test
    @DisplayName("제약 조건 위반 예외 처리")
    void handleConstraintViolationException_Success() {
        // Given
        when(messageService.getMessage("CONSTRAINT_VIOLATION")).thenReturn("제약 조건 위반이 발생했습니다.");
        
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("전화번호 형식이 올바르지 않습니다.");
        
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleConstraintViolationException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("CONSTRAINT_VIOLATION");
        assertThat(response.getBody().getMessage()).contains("제약 조건 위반");
        assertThat(response.getBody().getMessage()).contains("전화번호 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("필수 파라미터 누락 예외 처리")
    void handleMissingParameterException_Success() {
        // Given
        when(messageService.getMessage("MISSING_PARAMETER")).thenReturn("필수 파라미터가 누락되었습니다.");
        
        MissingServletRequestParameterException exception = 
                new MissingServletRequestParameterException("phoneNumber", "String");
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMissingParameterException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("MISSING_PARAMETER");
        assertThat(response.getBody().getMessage()).contains("필수 파라미터가 누락되었습니다");
        assertThat(response.getBody().getMessage()).contains("phoneNumber");
    }

    @Test
    @DisplayName("메서드 인자 타입 불일치 예외 처리")
    void handleTypeMismatchException_Success() {
        // Given
        when(messageService.getMessage("TYPE_MISMATCH")).thenReturn("잘못된 파라미터 타입입니다.");
        
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("coinCount");
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleTypeMismatchException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("TYPE_MISMATCH");
        assertThat(response.getBody().getMessage()).contains("잘못된 파라미터 타입입니다");
        assertThat(response.getBody().getMessage()).contains("coinCount");
    }

    @Test
    @DisplayName("HTTP 메서드 지원하지 않음 예외 처리")
    void handleMethodNotSupportedException_Success() {
        // Given
        when(messageService.getMessage("METHOD_NOT_ALLOWED")).thenReturn("지원하지 않는 HTTP 메서드입니다.");
        
        HttpRequestMethodNotSupportedException exception = 
                new HttpRequestMethodNotSupportedException("DELETE");
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMethodNotSupportedException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().getCode()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(response.getBody().getMessage()).contains("지원하지 않는 HTTP 메서드입니다");
        assertThat(response.getBody().getMessage()).contains("DELETE");
    }

    @Test
    @DisplayName("핸들러를 찾을 수 없음 예외 처리")
    void handleNoHandlerFoundException_Success() {
        // Given
        when(messageService.getMessage("NOT_FOUND")).thenReturn("요청한 리소스를 찾을 수 없습니다.");
        
        NoHandlerFoundException exception = 
                new NoHandlerFoundException("GET", "/api/nonexistent", null);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNoHandlerFoundException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("요청한 리소스를 찾을 수 없습니다");
        assertThat(response.getBody().getMessage()).contains("GET");
        assertThat(response.getBody().getMessage()).contains("/api/nonexistent");
    }

    @Test
    @DisplayName("HTTP 메시지 읽기 불가 예외 처리")
    void handleHttpMessageNotReadableException_Success() {
        // Given
        when(messageService.getMessage("MESSAGE_NOT_READABLE")).thenReturn("요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
        
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("MESSAGE_NOT_READABLE");
        assertThat(response.getBody().getMessage()).isEqualTo("요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
    }

    @Test
    @DisplayName("데이터베이스 접근 예외 처리")
    void handleDataAccessException_Success() {
        // Given
        when(messageService.getMessage("DATABASE_ERROR")).thenReturn("데이터베이스 오류가 발생했습니다.");
        
        DataAccessException exception = mock(DataAccessException.class);
        when(exception.getMessage()).thenReturn("Database connection failed");
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleDataAccessException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("DATABASE_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("데이터베이스 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("일반적인 예외 처리")
    void handleGeneralException_Success() {
        // Given
        when(messageService.getMessage("INTERNAL_SERVER_ERROR")).thenReturn("서버 내부 오류가 발생했습니다.");
        
        RuntimeException exception = new RuntimeException("Unexpected error");
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGeneralException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("클라이언트 IP 주소 추출 - X-Forwarded-For 헤더")
    void extractClientIpAddress_XForwardedFor() {
        // Given
        request.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
        CoinException.InsufficientCoinsException exception = new CoinException.InsufficientCoinsException();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleCoinException(exception, request);
        
        // Then - 로깅이 수행되고 응답이 정상적으로 반환되어야 함
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INSUFFICIENT_COINS");
    }

    @Test
    @DisplayName("클라이언트 IP 주소 추출 - X-Real-IP 헤더")
    void extractClientIpAddress_XRealIp() {
        // Given
        request.addHeader("X-Real-IP", "192.168.1.200");
        CoinException.InsufficientCoinsException exception = new CoinException.InsufficientCoinsException();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleCoinException(exception, request);
        
        // Then - 로깅이 수행되고 응답이 정상적으로 반환되어야 함
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INSUFFICIENT_COINS");
    }

    @Test
    @DisplayName("HTTP 상태 코드 결정 - NOT_FOUND")
    void determineHttpStatus_NotFound() {
        // Given
        EntryException.EntryNotFoundException exception = new EntryException.EntryNotFoundException();
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleEntryException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("HTTP 상태 코드 결정 - CONFLICT")
    void determineHttpStatus_Conflict() {
        // Given
        LotteryException.LotteryAlreadyCompletedException exception = new LotteryException.LotteryAlreadyCompletedException("1일권");
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleLotteryException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}