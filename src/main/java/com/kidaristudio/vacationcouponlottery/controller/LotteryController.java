package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.LotteryResult;
import com.kidaristudio.vacationcouponlottery.dto.Winner;
import com.kidaristudio.vacationcouponlottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 추첨 시스템 컨트롤러
 * 추첨 실행, 당첨자 조회 API를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    /**
     * 특정 쿠폰 타입 추첨 실행 API
     * 지정된 쿠폰 타입에 대해 추첨을 실행합니다.
     * 
     * @param couponType 추첨할 쿠폰 타입 (ONE_DAY/THREE_DAY)
     * @return 추첨 결과
     */
    @PostMapping("/conduct")
    public ResponseEntity<ApiResponse<LotteryResult>> conductLottery(
            @RequestParam CouponType couponType) {
        
        log.info("특정 쿠폰 타입 추첨 실행 요청 - 쿠폰 타입: {}", couponType);
        
        ApiResponse<LotteryResult> response = lotteryService.conductLottery(couponType);
        
        log.info("특정 쿠폰 타입 추첨 실행 완료 - 쿠폰 타입: {}, 결과: {}, 당첨자 수: {}", 
                couponType, response.getCode(), 
                response.getData() != null ? response.getData().getWinners().size() : 0);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 쿠폰 타입 일괄 추첨 실행 API
     * 모든 쿠폰 타입에 대해 일괄 추첨을 실행합니다.
     * 
     * @return 전체 추첨 결과 목록
     */
    @PostMapping("/conduct-all")
    public ResponseEntity<ApiResponse<List<LotteryResult>>> conductAllLotteries() {
        
        log.info("전체 쿠폰 타입 일괄 추첨 실행 요청");
        
        ApiResponse<List<LotteryResult>> response = lotteryService.conductAllLotteries();
        
        log.info("전체 쿠폰 타입 일괄 추첨 실행 완료 - 결과: {}, 추첨 결과 수: {}", 
                response.getCode(), response.getData() != null ? response.getData().size() : 0);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 당첨자 조회 API
     * 특정 쿠폰 타입 또는 전체 당첨자 목록을 조회합니다.
     * 
     * @param couponType 조회할 쿠폰 타입 (null인 경우 전체 조회)
     * @return 당첨자 목록
     */
    @GetMapping("/winners")
    public ResponseEntity<ApiResponse<List<Winner>>> getWinners(
            @RequestParam(required = false) CouponType couponType) {
        
        log.info("당첨자 조회 요청 - 쿠폰 타입: {}", couponType != null ? couponType : "전체");
        
        ApiResponse<List<Winner>> response = lotteryService.getWinners(couponType);
        
        log.info("당첨자 조회 완료 - 쿠폰 타입: {}, 당첨자 수: {}", 
                couponType != null ? couponType : "전체", 
                response.getData() != null ? response.getData().size() : 0);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 추첨 완료 여부 확인 API
     * 특정 쿠폰 타입 또는 전체 추첨 완료 여부를 확인합니다.
     * 
     * @param couponType 확인할 쿠폰 타입 (null인 경우 전체 확인)
     * @return 추첨 완료 여부
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> isLotteryCompleted(
            @RequestParam(required = false) CouponType couponType) {
        
        log.info("추첨 완료 여부 확인 요청 - 쿠폰 타입: {}", couponType != null ? couponType : "전체");
        
        ApiResponse<Boolean> response = lotteryService.isLotteryCompleted(couponType);
        
        log.info("추첨 완료 여부 확인 완료 - 쿠폰 타입: {}, 완료 여부: {}", 
                couponType != null ? couponType : "전체", response.getData());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 추첨 결과 초기화 API (개발/테스트 용도)
     * 특정 쿠폰 타입 또는 전체 추첨 결과를 초기화합니다.
     * 
     * @param couponType 초기화할 쿠폰 타입 (null인 경우 전체 초기화)
     * @return 초기화 결과
     */
    @DeleteMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetLottery(
            @RequestParam(required = false) CouponType couponType) {
        
        log.info("추첨 결과 초기화 요청 - 쿠폰 타입: {}", couponType != null ? couponType : "전체");
        
        ApiResponse<Void> response = lotteryService.resetLottery(couponType);
        
        log.info("추첨 결과 초기화 완료 - 쿠폰 타입: {}, 결과: {}", 
                couponType != null ? couponType : "전체", response.getCode());
        
        return ResponseEntity.ok(response);
    }
}