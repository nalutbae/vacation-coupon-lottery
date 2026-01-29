package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.*;
import com.kidaristudio.vacationcouponlottery.service.VacationCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 휴가 쿠폰 응모 컨트롤러
 * 응모 등록, 취소, 현황 조회 API를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class VacationCouponController {

    private final VacationCouponService vacationCouponService;

    /**
     * 휴가 쿠폰 응모 API
     * 사용자의 응모 코인을 차감하고 해당 쿠폰에 응모를 등록합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @param couponType 쿠폰 타입 (ONE_DAY/THREE_DAY)
     * @param coinCount 사용할 응모 코인 수
     * @return 응모 결과
     */
    @PostMapping("/enter")
    public ResponseEntity<ApiResponse<EntryResult>> enterLottery(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber,
            
            @RequestParam 
            @NotNull(message = "쿠폰 타입은 필수입니다")
            CouponType couponType,
            
            @RequestParam 
            @Positive(message = "코인 수는 1 이상이어야 합니다")
            int coinCount) {
        
        log.info("휴가 쿠폰 응모 요청 - 전화번호: {}, 쿠폰 타입: {}, 코인 수: {}", 
                phoneNumber, couponType, coinCount);
        
        ApiResponse<EntryResult> response = vacationCouponService.enterLottery(phoneNumber, couponType, coinCount);
        
        log.info("휴가 쿠폰 응모 완료 - 전화번호: {}, 결과: {}, 응모 ID: {}", 
                phoneNumber, response.getCode(), 
                response.getData() != null ? response.getData().getEntryId() : "N/A");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 휴가 쿠폰 응모 취소 API
     * 응모를 취소하고 사용한 응모 코인을 반환합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @param entryId 취소할 응모 ID
     * @return 취소 결과
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<ApiResponse<CancelResult>> cancelEntry(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber,
            
            @RequestParam 
            @NotNull(message = "응모 ID는 필수입니다")
            @Positive(message = "응모 ID는 양수여야 합니다")
            Long entryId) {
        
        log.info("휴가 쿠폰 응모 취소 요청 - 전화번호: {}, 응모 ID: {}", phoneNumber, entryId);
        
        ApiResponse<CancelResult> response = vacationCouponService.cancelEntry(phoneNumber, entryId);
        
        log.info("휴가 쿠폰 응모 취소 완료 - 전화번호: {}, 응모 ID: {}, 결과: {}", 
                phoneNumber, entryId, response.getCode());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 응모 현황 조회 API
     * 특정 사용자의 모든 응모 내역을 조회합니다.
     * 
     * @param phoneNumber 사용자 전화번호
     * @return 사용자 응모 현황 목록
     */
    @GetMapping("/my-entries")
    public ResponseEntity<ApiResponse<List<UserEntryStatus>>> getUserEntries(
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber) {
        
        log.info("사용자 응모 현황 조회 요청 - 전화번호: {}", phoneNumber);
        
        ApiResponse<List<UserEntryStatus>> response = vacationCouponService.getUserEntries(phoneNumber);
        
        log.info("사용자 응모 현황 조회 완료 - 전화번호: {}, 응모 수: {}", 
                phoneNumber, response.getData() != null ? response.getData().size() : 0);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 휴가 쿠폰별 전체 응모 현황 조회 API
     * 모든 쿠폰 타입의 전체 응모 통계를 조회합니다.
     * 
     * @return 쿠폰별 응모 현황 목록
     */
    @GetMapping("/all-entries")
    public ResponseEntity<ApiResponse<List<CouponEntryStatus>>> getAllEntries() {
        
        log.info("전체 응모 현황 조회 요청");
        
        ApiResponse<List<CouponEntryStatus>> response = vacationCouponService.getAllEntries();
        
        log.info("전체 응모 현황 조회 완료 - 쿠폰 타입 수: {}", 
                response.getData() != null ? response.getData().size() : 0);
        
        return ResponseEntity.ok(response);
    }
}