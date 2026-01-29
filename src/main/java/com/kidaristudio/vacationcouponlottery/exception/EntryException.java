package com.kidaristudio.vacationcouponlottery.exception;

/**
 * 휴가 쿠폰 응모 관련 예외 클래스들
 * 응모, 취소, 조회 과정에서 발생하는 다양한 예외를 정의합니다.
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

        public EntryNotFoundException(String message) {
            super("ENTRY_NOT_FOUND", message);
        }
    }

    /**
     * 잘못된 요청인 경우
     */
    public static class InvalidRequestException extends BaseEntryException {
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

        public WinnerCannotCancelException(String message) {
            super("WINNER_CANNOT_CANCEL", message);
        }
    }

    /**
     * 응모 처리 중 일반적인 오류
     */
    public static class EntryProcessingException extends BaseEntryException {
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
        public NotLotteryPeriodException() {
            super("NOT_LOTTERY_PERIOD", "추첨 기간이 아닙니다.");
        }

        public NotLotteryPeriodException(String message) {
            super("NOT_LOTTERY_PERIOD", message);
        }
    }

    /**
     * 응모 기간이 종료된 경우
     */
    public static class EntryPeriodEndedException extends BaseEntryException {
        public EntryPeriodEndedException() {
            super("ENTRY_PERIOD_ENDED", "응모 기간이 종료되었습니다.");
        }

        public EntryPeriodEndedException(String message) {
            super("ENTRY_PERIOD_ENDED", message);
        }
    }
}