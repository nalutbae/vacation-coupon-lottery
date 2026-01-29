package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.dto.CouponEntryStatus;
import com.kidaristudio.vacationcouponlottery.dto.UserCoinInfo;
import com.kidaristudio.vacationcouponlottery.dto.UserEntryStatus;
import com.kidaristudio.vacationcouponlottery.service.StatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * 현황 조회 컨트롤러
 * 개인/전체 응모 현황, 코인 현황, 시스템 통계 조회 API를 제공합니다.
 */
@Tag(name = "현황 조회", description = "개인/전체 응모 현황, 코인 현황 및 시스템 통계 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    /**
     * 사용자 개인 응모 현황 조회 API
     * 특정 사용자의 모든 응모 내역을 조회합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 개인 응모 현황 목록
     */
    @GetMapping("/user/entries")
    public ResponseEntity<ApiResponse<List<UserEntryStatus>>> getUserEntryStatus(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber) {
        
        log.info("사용자 개인 응모 현황 조회 요청 - 전화번호: {}", phoneNumber);
        
        ApiResponse<List<UserEntryStatus>> response = statusService.getUserEntryStatus(phoneNumber);
        
        log.info("사용자 개인 응모 현황 조회 완료 - 전화번호: {}, 응모 수: {}", 
                phoneNumber, response.getData() != null ? response.getData().size() : 0);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 응모 코인 수량 조회 API
     * 특정 사용자의 현재 보유 코인 수를 조회합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 보유 응모 코인 수
     */
    @GetMapping("/user/coins")
    public ResponseEntity<ApiResponse<Integer>> getUserCoinCount(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber) {
        
        log.info("사용자 코인 수량 조회 요청 - 전화번호: {}", phoneNumber);
        
        ApiResponse<Integer> response = statusService.getUserCoinCount(phoneNumber);
        
        log.info("사용자 코인 수량 조회 완료 - 전화번호: {}, 코인 수: {}", 
                phoneNumber, response.getData());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 코인 정보 조회 API
     * 특정 사용자의 코인 정보(보유 코인 + 누적 획득 코인)를 조회합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 사용자 코인 정보
     */
    @GetMapping("/user/coin-info")
    public ResponseEntity<ApiResponse<UserCoinInfo>> getUserCoinInfo(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber) {
        
        log.info("사용자 코인 정보 조회 요청 - 전화번호: {}", phoneNumber);
        
        ApiResponse<UserCoinInfo> response = statusService.getUserCoinInfo(phoneNumber);
        
        log.info("사용자 코인 정보 조회 완료 - 전화번호: {}, 보유 코인: {}, 누적 획득: {}", 
                phoneNumber, 
                response.getData() != null ? response.getData().getCoinCount() : "N/A",
                response.getData() != null ? response.getData().getTotalAcquiredCoins() : "N/A");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 응모 현황 조회 API
     * 모든 쿠폰 타입의 전체 응모 통계를 조회합니다.
     * 
     * @return 쿠폰별 전체 응모 현황
     */
    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<List<CouponEntryStatus>>> getAllEntryStatus() {
        
        log.info("전체 응모 현황 조회 요청");
        
        ApiResponse<List<CouponEntryStatus>> response = statusService.getAllEntryStatus();
        
        log.info("전체 응모 현황 조회 완료 - 쿠폰 타입 수: {}", 
                response.getData() != null ? response.getData().size() : 0);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 응모 코인 현황 조회 API
     * 시스템 전체의 코인 분배 현황을 조회합니다.
     * 
     * @return 전체 코인 현황
     */
    @GetMapping("/coins")
    public ResponseEntity<ApiResponse<CoinStatusResponse>> getAllCoinStatus() {
        
        log.info("전체 코인 현황 조회 요청");
        
        ApiResponse<CoinStatusResponse> response = statusService.getAllCoinStatus();
        
        log.info("전체 코인 현황 조회 완료 - 남은 코인: {}", 
                response.getData() != null ? response.getData().getRemainingCoins() : "N/A");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 시스템 전체 통계 조회 API
     * 시스템 전반의 통계 정보를 조회합니다.
     * 
     * @return 시스템 전체 통계 정보
     */
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<StatusService.SystemStatistics>> getSystemStatistics() {
        
        log.info("시스템 전체 통계 조회 요청");
        
        ApiResponse<StatusService.SystemStatistics> response = statusService.getSystemStatistics();
        
        log.info("시스템 전체 통계 조회 완료 - 전체 사용자 수: {}, 전체 응모 수: {}", 
                response.getData() != null ? response.getData().getTotalUsers() : "N/A",
                response.getData() != null ? response.getData().getTotalEntries() : "N/A");
        
        return ResponseEntity.ok(response);
    }
}