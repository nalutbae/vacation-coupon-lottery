package com.kidaristudio.vacationcouponlottery.exception;

import com.kidaristudio.vacationcouponlottery.service.MessageService;

/**
 * 휴가 쿠폰 응모 관련 예외 클래스들
 * 응모, 취소, 조회 과정에서 발생하는 다양한 예외를 정의합니다.
 * MessageService를 통해 국제화된 메시지를 제공합니다.
 */
public class EntryException {

    /**
     * 기본 응모 예외 클래스
     */
    public static abstract class BaseEntryException extends BusinessException {
        public BaseEntryException(String errorCode, String errorMessage) {
            super(errorCode, errorMessage);
        }

        public BaseEntryException(String errorCode, String errorMessage, Throwable cause) {
            super(errorCode, errorMessage, cause);
        }
    }

    /**
     * 사용자를 찾을 수 없는 경우
     */
    public static class UserNotFoundException extends BaseEntryException {
        public UserNotFoundException() {
            super("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }
        
        public UserNotFoundException(MessageService messageService) {
            super("USER_NOT_FOUND", messageService.getMessage("USER_NOT_FOUND"));
        }

        public UserNotFoundException(String message) {
            super("USER_NOT_FOUND", message);
        }
    }

    /**
     * 응모 내역을 찾을 수 없는 경우
     */
    public static class EntryNotFoundException extends BaseEntryException {
        public EntryNotFoundException() {
            super("ENTRY_NOT_FOUND", "응모 내역을 찾을 수 없습니다.");
        }
        
        public EntryNotFoundException(MessageService messageService) {
            super("ENTRY_NOT_FOUND", messageService.getMessage("ENTRY_NOT_FOUND"));
        }

        public EntryNotFoundException(String message) {
            super("ENTRY_NOT_FOUND", message);
        }
    }

    /**
     * 잘못된 요청인 경우
     */
    public static class InvalidRequestException extends BaseEntryException {
        public InvalidRequestException() {
            super("INVALID_REQUEST", "유효하지 않은 응모 요청입니다.");
        }
        
        public InvalidRequestException(MessageService messageService) {
            super("INVALID_REQUEST", messageService.getMessage("INVALID_REQUEST"));
        }
        
        public InvalidRequestException(String message) {
            super("INVALID_REQUEST", message);
        }
    }

    /**
     * 권한이 없는 접근인 경우
     */
    public static class UnauthorizedAccessException extends BaseEntryException {
        public UnauthorizedAccessException() {
            super("UNAUTHORIZED_ACCESS", "권한이 없습니다.");
        }
        
        public UnauthorizedAccessException(MessageService messageService) {
            super("UNAUTHORIZED_ACCESS", messageService.getMessage("UNAUTHORIZED_ACCESS"));
        }

        public UnauthorizedAccessException(String message) {
            super("UNAUTHORIZED_ACCESS", message);
        }
    }

    /**
     * 이미 취소된 응모인 경우
     */
    public static class AlreadyCancelledException extends BaseEntryException {
        public AlreadyCancelledException() {
            super("ALREADY_CANCELLED", "이미 취소된 응모입니다.");
        }
        
        public AlreadyCancelledException(MessageService messageService) {
            super("ALREADY_CANCELLED", messageService.getMessage("ALREADY_CANCELLED"));
        }

        public AlreadyCancelledException(String message) {
            super("ALREADY_CANCELLED", message);
        }
    }

    /**
     * 당첨자는 응모를 취소할 수 없는 경우
     */
    public static class WinnerCannotCancelException extends BaseEntryException {
        public WinnerCannotCancelException() {
            super("WINNER_CANNOT_CANCEL", "당첨된 응모는 취소할 수 없습니다.");
        }
        
        public WinnerCannotCancelException(MessageService messageService) {
            super("WINNER_CANNOT_CANCEL", messageService.getMessage("WINNER_CANNOT_CANCEL"));
        }

        public WinnerCannotCancelException(String message) {
            super("WINNER_CANNOT_CANCEL", message);
        }
    }

    /**
     * 응모 처리 중 일반적인 오류
     */
    public static class EntryProcessingException extends BaseEntryException {
        public EntryProcessingException(MessageService messageService) {
            super("ENTRY_PROCESSING_ERROR", messageService.getMessage("ENTRY_PROCESSING_ERROR"));
        }
        
        public EntryProcessingException(String message) {
            super("ENTRY_PROCESSING_ERROR", message);
        }

        public EntryProcessingException(String message, Throwable cause) {
            super("ENTRY_PROCESSING_ERROR", message, cause);
        }
    }

    /**
     * 추첨 기간이 아닌 경우
     */
    public static class NotLotteryPeriodException extends BaseEntryException {
        public NotLotteryPeriodException(MessageService messageService) {
            super("NOT_LOTTERY_PERIOD", messageService.getMessage("NOT_LOTTERY_PERIOD"));
        }

        public NotLotteryPeriodException(String message) {
            super("NOT_LOTTERY_PERIOD", message);
        }
    }

    /**
     * 응모 기간이 종료된 경우
     */
    public static class EntryPeriodEndedException extends BaseEntryException {
        public EntryPeriodEndedException(MessageService messageService) {
            super("ENTRY_PERIOD_ENDED", messageService.getMessage("ENTRY_PERIOD_ENDED"));
        }

        public EntryPeriodEndedException(String message) {
            super("ENTRY_PERIOD_ENDED", message);
        }
    }
}