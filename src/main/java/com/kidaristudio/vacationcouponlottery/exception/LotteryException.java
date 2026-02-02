package com.kidaristudio.vacationcouponlottery.exception;

import com.kidaristudio.vacationcouponlottery.service.MessageService;

/**
 * 추첨 관련 예외 클래스들
 * 추첨 프로세스에서 발생할 수 있는 다양한 예외 상황을 정의합니다.
 * MessageService를 통해 국제화된 메시지를 제공합니다.
 */
public class LotteryException {

    /**
     * 추첨 기본 예외
     */
    public static abstract class BaseLotteryException extends BusinessException {
        public BaseLotteryException(String errorCode, String errorMessage) {
            super(errorCode, errorMessage);
        }

        public BaseLotteryException(String errorCode, String errorMessage, Throwable cause) {
            super(errorCode, errorMessage, cause);
        }
    }

    /**
     * 이미 추첨이 완료된 경우
     */
    public static class LotteryAlreadyCompletedException extends BaseLotteryException {
        public LotteryAlreadyCompletedException(MessageService messageService) {
            super("LOTTERY_ALREADY_COMPLETED", messageService.getMessage("LOTTERY_ALREADY_COMPLETED"));
        }
        
        public LotteryAlreadyCompletedException(String couponType) {
            super("LOTTERY_ALREADY_COMPLETED", 
                  String.format("%s 쿠폰에 대한 추첨이 이미 완료되었습니다.", couponType));
        }
    }

    /**
     * 응모자가 없어 추첨을 진행할 수 없는 경우
     */
    public static class NoEntrantsException extends BaseLotteryException {
        public NoEntrantsException(MessageService messageService) {
            super("NO_ENTRANTS", messageService.getMessage("NO_ENTRANTS"));
        }
        
        public NoEntrantsException(String couponType) {
            super("NO_ENTRANTS", 
                  String.format("%s 쿠폰에 대한 응모자가 없어 추첨을 진행할 수 없습니다.", couponType));
        }
    }

    /**
     * 추첨 처리 중 오류가 발생한 경우
     */
    public static class LotteryProcessingException extends BaseLotteryException {
        public LotteryProcessingException(MessageService messageService) {
            super("LOTTERY_PROCESSING_ERROR", messageService.getMessage("LOTTERY_PROCESSING_ERROR"));
        }
        
        public LotteryProcessingException(String message) {
            super("LOTTERY_PROCESSING_ERROR", "추첨 처리 중 오류가 발생했습니다: " + message);
        }

        public LotteryProcessingException(String message, Throwable cause) {
            super("LOTTERY_PROCESSING_ERROR", "추첨 처리 중 오류가 발생했습니다: " + message, cause);
        }
    }

    /**
     * 추첨 결과를 찾을 수 없는 경우
     */
    public static class LotteryResultNotFoundException extends BaseLotteryException {
        public LotteryResultNotFoundException(MessageService messageService) {
            super("LOTTERY_RESULT_NOT_FOUND", messageService.getMessage("LOTTERY_RESULT_NOT_FOUND"));
        }
        
        public LotteryResultNotFoundException(String couponType) {
            super("LOTTERY_RESULT_NOT_FOUND", 
                  String.format("%s 쿠폰에 대한 추첨 결과를 찾을 수 없습니다.", couponType));
        }
    }

    /**
     * 잘못된 추첨 요청인 경우
     */
    public static class InvalidLotteryRequestException extends BaseLotteryException {
        public InvalidLotteryRequestException(MessageService messageService) {
            super("INVALID_LOTTERY_REQUEST", messageService.getMessage("INVALID_LOTTERY_REQUEST"));
        }
        
        public InvalidLotteryRequestException(String message) {
            super("INVALID_LOTTERY_REQUEST", "잘못된 추첨 요청입니다: " + message);
        }
    }

    /**
     * 추첨 권한이 없는 경우
     */
    public static class LotteryPermissionDeniedException extends BaseLotteryException {
        public LotteryPermissionDeniedException(MessageService messageService) {
            super("LOTTERY_PERMISSION_DENIED", messageService.getMessage("LOTTERY_PERMISSION_DENIED"));
        }
        
        public LotteryPermissionDeniedException() {
            super("LOTTERY_PERMISSION_DENIED", "추첨을 실행할 권한이 없습니다.");
        }
    }
}