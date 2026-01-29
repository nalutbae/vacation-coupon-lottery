package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.*;
import com.kidaristudio.vacationcouponlottery.service.VacationCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "휴가 쿠폰 응모", description = "휴가 쿠폰 응모, 취소 및 현황 조회 API")
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
    @Operation(
        summary = "휴가 쿠폰 응모",
        description = "사용자의 응모 코인을 차감하고 해당 쿠폰에 응모를 등록합니다. 코인 수에 따라 당첨 확률이 증가합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "응모 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "휴가 쿠폰 응모가 완료되었습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": {
                            "entryId": 12345,
                            "phoneNumber": "010-1234-5678",
                            "couponType": "ONE_DAY",
                            "usedCoins": 2,
                            "entryTime": "2024-01-29T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "응모 실패 (코인 부족, 잘못된 요청 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "INSUFFICIENT_COINS",
                        "message": "응모 코인이 부족합니다",
                        "time": "2024-01-29T10:30:00",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/enter")
    public ResponseEntity<ApiResponse<EntryResult>> enterLottery(
            @Parameter(
                description = "사용자 전화번호 (010-1234-5678 형식)", 
                required = true,
                example = "010-1234-5678"
            )
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber,
            
            @Parameter(
                description = "쿠폰 타입", 
                required = true,
                example = "ONE_DAY"
            )
            @RequestParam 
            @NotNull(message = "쿠폰 타입은 필수입니다")
            CouponType couponType,
            
            @Parameter(
                description = "사용할 응모 코인 수 (1-3개)", 
                required = true,
                example = "2"
            )
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
    @Operation(
        summary = "휴가 쿠폰 응모 취소",
        description = "응모를 취소하고 사용한 응모 코인을 반환합니다. 이미 추첨이 완료된 응모는 취소할 수 없습니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "취소 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "응모가 성공적으로 취소되었습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": {
                            "entryId": 12345,
                            "phoneNumber": "010-1234-5678",
                            "returnedCoins": 2,
                            "cancelTime": "2024-01-29T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "취소 실패 (이미 취소됨, 당첨자는 취소 불가 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "ALREADY_CANCELLED",
                        "message": "이미 취소된 응모입니다",
                        "time": "2024-01-29T10:30:00",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/cancel")
    public ResponseEntity<ApiResponse<CancelResult>> cancelEntry(
            @Parameter(
                description = "사용자 전화번호 (010-1234-5678 형식)", 
                required = true,
                example = "010-1234-5678"
            )
            @RequestParam 
            @NotBlank(message = "전화번호는 필수입니다")
            @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
            String phoneNumber,
            
            @Parameter(
                description = "취소할 응모 ID", 
                required = true,
                example = "12345"
            )
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
    @Operation(
        summary = "사용자 응모 현황 조회",
        description = "특정 사용자의 모든 응모 내역을 조회합니다. 활성/취소 상태, 사용 코인 수, 응모 시간 등을 포함합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "사용자 응모 현황을 성공적으로 조회했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": [
                            {
                                "entryId": 12345,
                                "couponType": "ONE_DAY",
                                "usedCoins": 2,
                                "isActive": true,
                                "entryTime": "2024-01-29T10:30:00"
                            },
                            {
                                "entryId": 12346,
                                "couponType": "THREE_DAY",
                                "usedCoins": 3,
                                "isActive": false,
                                "entryTime": "2024-01-29T09:15:00"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/my-entries")
    public ResponseEntity<ApiResponse<List<UserEntryStatus>>> getUserEntries(
            @Parameter(
                description = "사용자 전화번호 (010-1234-5678 형식)", 
                required = true,
                example = "010-1234-5678"
            )
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
    @Operation(
        summary = "전체 응모 현황 조회",
        description = "모든 쿠폰 타입의 전체 응모 통계를 조회합니다. 쿠폰별 총 응모 수, 활성 응모 수 등을 포함합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "전체 응모 현황을 성공적으로 조회했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": [
                            {
                                "couponType": "ONE_DAY",
                                "totalEntries": 150,
                                "activeEntries": 120,
                                "totalCoinsUsed": 280
                            },
                            {
                                "couponType": "THREE_DAY",
                                "totalEntries": 80,
                                "activeEntries": 75,
                                "totalCoinsUsed": 200
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/all-entries")
    public ResponseEntity<ApiResponse<List<CouponEntryStatus>>> getAllEntries() {
        
        log.info("전체 응모 현황 조회 요청");
        
        ApiResponse<List<CouponEntryStatus>> response = vacationCouponService.getAllEntries();
        
        log.info("전체 응모 현황 조회 완료 - 쿠폰 타입 수: {}", 
                response.getData() != null ? response.getData().size() : 0);
        
        return ResponseEntity.ok(response);
    }
}