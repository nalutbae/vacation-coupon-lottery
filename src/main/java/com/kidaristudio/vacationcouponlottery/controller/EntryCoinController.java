package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 응모 코인 관리 컨트롤러
 * 코인 획득, 수량 조회, 전체 현황 조회 API를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class EntryCoinController {

    private final EntryCoinService entryCoinService;

    /**
     * 응모 코인 획득 API
     * 선착순으로 응모 코인 1개를 제공합니다.
     * 
     * @param phoneNumber 사용자 전화번호 (010-1234-5678 형식)
     * @return 코인 획득 결과
     */
    @PostMapping("/acquire")
    public ResponseEntity<ApiResponse<CoinAcquisitionResult>> acquireCoin(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber) {
        
        log.info("응모 코인 획득 요청 - 전화번호: {}", phoneNumber);
        
        ApiResponse<CoinAcquisitionResult> response = entryCoinService.acquireCoin(phoneNumber);
        
        log.info("응모 코인 획득 완료 - 전화번호: {}, 결과: {}", phoneNumber, response.getCode());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 응모 코인 수량 조회 API
     * 특정 사용자의 현재 보유 코인 수를 반환합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 보유 코인 수
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getUserCoinCount(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber) {
        
        log.info("사용자 코인 수량 조회 요청 - 전화번호: {}", phoneNumber);
        
        ApiResponse<Integer> response = entryCoinService.getUserCoinCount(phoneNumber);
        
        log.info("사용자 코인 수량 조회 완료 - 전화번호: {}, 코인 수: {}", phoneNumber, response.getData());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 응모 코인 현황 조회 API
     * 시스템 전체의 코인 분배 현황을 제공합니다.
     * 
     * @return 전체 코인 현황
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<CoinStatusResponse>> getAllCoinStatus() {
        
        log.info("전체 코인 현황 조회 요청");
        
        ApiResponse<CoinStatusResponse> response = entryCoinService.getAllCoinStatus();
        
        log.info("전체 코인 현황 조회 완료 - 남은 코인: {}", 
                response.getData() != null ? response.getData().getRemainingCoins() : "N/A");
        
        return ResponseEntity.ok(response);
    }
}