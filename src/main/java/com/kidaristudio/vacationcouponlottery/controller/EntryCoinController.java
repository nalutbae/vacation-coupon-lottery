package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
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

/**
 * 응모 코인 관리 컨트롤러
 * 코인 획득, 수량 조회, 전체 현황 조회 API를 제공합니다.
 */
@Tag(name = "응모 코인 관리", description = "응모 코인 획득, 조회 및 현황 관리 API")
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
    @Operation(
        summary = "응모 코인 획득",
        description = "선착순으로 응모 코인 1개를 제공합니다. 일일 한도와 전체 한도가 적용됩니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "코인 획득 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "코인을 성공적으로 획득했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": {
                            "phoneNumber": "010-1234-5678",
                            "coinCount": 3,
                            "acquisitionTime": "2024-01-29T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "코인 획득 실패 (한도 초과, 수량 부족 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "COIN_LIMIT_EXCEEDED",
                        "message": "일일 코인 획득 한도를 초과했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/acquire")
    public ResponseEntity<ApiResponse<CoinAcquisitionResult>> acquireCoin(
            @Parameter(
                description = "사용자 전화번호 (010-1234-5678 형식)", 
                required = true,
                example = "010-1234-5678"
            )
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
    @Operation(
        summary = "사용자 코인 수량 조회",
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
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "USER_NOT_FOUND",
                        "message": "해당 전화번호의 사용자를 찾을 수 없습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/count")
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
    @Operation(
        summary = "전체 코인 현황 조회",
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
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<CoinStatusResponse>> getAllCoinStatus() {
        
        log.info("전체 코인 현황 조회 요청");
        
        ApiResponse<CoinStatusResponse> response = entryCoinService.getAllCoinStatus();
        
        log.info("전체 코인 현황 조회 완료 - 남은 코인: {}", 
                response.getData() != null ? response.getData().getRemainingCoins() : "N/A");
        
        return ResponseEntity.ok(response);
    }
}