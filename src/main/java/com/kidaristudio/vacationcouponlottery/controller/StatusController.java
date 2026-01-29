package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.dto.CouponEntryStatus;
import com.kidaristudio.vacationcouponlottery.dto.UserEntryStatus;
import com.kidaristudio.vacationcouponlottery.service.StatusService;
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
    @Operation(
        summary = "사용자 개인 응모 현황 조회",
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
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/user/entries")
    public ResponseEntity<ApiResponse<List<UserEntryStatus>>> getUserEntryStatus(
            @Parameter(
                description = "사용자 전화번호 (010-1234-5678 형식)", 
                required = true,
                example = "010-1234-5678"
            )
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
    @Operation(
        summary = "사용자 응모 코인 수량 조회",
        description = "특정 사용자의 현재 보유 코인 수를 조회합니다."
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
                        "message": "코인 수량을 성공적으로 조회했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": 5
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/user/coins")
    public ResponseEntity<ApiResponse<Integer>> getUserCoinCount(
            @Parameter(
                description = "사용자 전화번호 (010-1234-5678 형식)", 
                required = true,
                example = "010-1234-5678"
            )
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
     * 전체 응모 현황 조회 API
     * 모든 쿠폰 타입의 전체 응모 통계를 조회합니다.
     * 
     * @return 쿠폰별 전체 응모 현황
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
    @Operation(
        summary = "전체 응모 코인 현황 조회",
        description = "시스템 전체의 코인 분배 현황을 조회합니다. 총 코인 수, 분배된 코인 수, 남은 코인 수 등을 포함합니다."
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
                        "message": "전체 코인 현황을 성공적으로 조회했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": {
                            "totalCoins": 1000,
                            "distributedCoins": 750,
                            "remainingCoins": 250,
                            "totalUsers": 150
                        }
                    }
                    """
                )
            )
        )
    })
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
    @Operation(
        summary = "시스템 전체 통계 조회",
        description = "시스템 전반의 통계 정보를 조회합니다. 전체 사용자 수, 전체 응모 수, 코인 현황 등을 포함합니다."
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
                        "message": "시스템 통계를 성공적으로 조회했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": {
                            "totalUsers": 200,
                            "totalEntries": 350,
                            "totalActiveEntries": 300,
                            "totalCoinsDistributed": 800,
                            "totalCoinsUsed": 600,
                            "totalWinners": 25
                        }
                    }
                    """
                )
            )
        )
    })
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