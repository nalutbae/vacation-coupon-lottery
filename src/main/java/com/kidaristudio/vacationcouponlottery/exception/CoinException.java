package com.kidaristudio.vacationcouponlottery.exception;

/**
 * 응모 코인 관련 예외 클래스들
 * 코인 획득, 사용, 관리 과정에서 발생하는 예외를 정의합니다.
 */
public class CoinException {

    /**
     * 응모 코인 부족 예외
     */
    public static class InsufficientCoinsException extends BusinessException {
        public InsufficientCoinsException() {
            super("INSUFFICIENT_COINS", "응모 코인이 부족합니다.");
        }
        
        public InsufficientCoinsException(String message) {
            super("INSUFFICIENT_COINS", message);
        }
    }

    /**
     * 응모 코인 한도 초과 예외
     */
    public static class CoinLimitExceededException extends BusinessException {
        public CoinLimitExceededException() {
            super("COIN_LIMIT_EXCEEDED", "응모 코인 한도를 초과했습니다. (최대 3개)");
        }
        
        public CoinLimitExceededException(String message) {
            super("COIN_LIMIT_EXCEEDED", message);
        }
    }

    /**
     * 응모 코인 소진 예외
     */
    public static class NoRemainingCoinsException extends BusinessException {
        public NoRemainingCoinsException() {
            super("NO_REMAINING_COINS", "응모 코인이 모두 소진되었습니다.");
        }
        
        public NoRemainingCoinsException(String message) {
            super("NO_REMAINING_COINS", message);
        }
    }

    /**
     * 응모 코인 획득 실패 예외
     */
    public static class CoinAcquisitionFailedException extends BusinessException {
        public CoinAcquisitionFailedException() {
            super("COIN_ACQUISITION_FAILED", "응모 코인 획득에 실패했습니다.");
        }
        
        public CoinAcquisitionFailedException(String message) {
            super("COIN_ACQUISITION_FAILED", message);
        }
        
        public CoinAcquisitionFailedException(String message, Throwable cause) {
            super("COIN_ACQUISITION_FAILED", message, cause);
        }
    }
}