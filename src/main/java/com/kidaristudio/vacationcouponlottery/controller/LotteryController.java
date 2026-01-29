package com.kidaristudio.vacationcouponlottery.controller;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.LotteryResult;
import com.kidaristudio.vacationcouponlottery.dto.Winner;
import com.kidaristudio.vacationcouponlottery.service.LotteryService;
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

import java.util.List;

/**
 * 추첨 시스템 컨트롤러
 * 추첨 실행, 당첨자 조회 API를 제공합니다.
 */
@Tag(name = "추첨 시스템", description = "휴가 쿠폰 추첨 실행 및 당첨자 조회 API")
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
    @Operation(
        summary = "특정 쿠폰 타입 추첨 실행",
        description = "지정된 쿠폰 타입에 대해 추첨을 실행합니다. Fisher-Yates 셔플 알고리즘을 사용하여 공정한 추첨을 보장합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "추첨 실행 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "추첨이 성공적으로 완료되었습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": {
                            "couponType": "ONE_DAY",
                            "totalEntries": 150,
                            "winnerCount": 10,
                            "winners": [
                                {
                                    "phoneNumber": "010-1234-5678",
                                    "couponType": "ONE_DAY",
                                    "winTime": "2024-01-29T10:30:00"
                                }
                            ],
                            "lotteryTime": "2024-01-29T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "추첨 실행 실패 (이미 완료됨, 응모자 없음 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "LOTTERY_ALREADY_COMPLETED",
                        "message": "이미 추첨이 완료되었습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/conduct")
    public ResponseEntity<ApiResponse<LotteryResult>> conductLottery(
            @Parameter(
                description = "추첨할 쿠폰 타입", 
                required = true,
                example = "ONE_DAY"
            )
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
    @Operation(
        summary = "전체 쿠폰 타입 일괄 추첨 실행",
        description = "모든 쿠폰 타입에 대해 일괄 추첨을 실행합니다. 각 쿠폰 타입별로 독립적으로 추첨이 진행됩니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "일괄 추첨 실행 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "전체 추첨이 성공적으로 완료되었습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": [
                            {
                                "couponType": "ONE_DAY",
                                "totalEntries": 150,
                                "winnerCount": 10,
                                "winners": [...],
                                "lotteryTime": "2024-01-29T10:30:00"
                            },
                            {
                                "couponType": "THREE_DAY",
                                "totalEntries": 80,
                                "winnerCount": 5,
                                "winners": [...],
                                "lotteryTime": "2024-01-29T10:30:00"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
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
    @Operation(
        summary = "당첨자 조회",
        description = "특정 쿠폰 타입 또는 전체 당첨자 목록을 조회합니다. 쿠폰 타입을 지정하지 않으면 모든 당첨자를 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "당첨자 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "당첨자 목록을 성공적으로 조회했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": [
                            {
                                "phoneNumber": "010-1234-5678",
                                "couponType": "ONE_DAY",
                                "winTime": "2024-01-29T10:30:00"
                            },
                            {
                                "phoneNumber": "010-9876-5432",
                                "couponType": "THREE_DAY",
                                "winTime": "2024-01-29T10:30:00"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/winners")
    public ResponseEntity<ApiResponse<List<Winner>>> getWinners(
            @Parameter(
                description = "조회할 쿠폰 타입 (지정하지 않으면 전체 조회)", 
                required = false,
                example = "ONE_DAY"
            )
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
    @Operation(
        summary = "추첨 완료 여부 확인",
        description = "특정 쿠폰 타입 또는 전체 추첨 완료 여부를 확인합니다. 쿠폰 타입을 지정하지 않으면 모든 쿠폰 타입의 추첨 완료 여부를 확인합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "추첨 상태 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "추첨 완료 여부를 성공적으로 조회했습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": true
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> isLotteryCompleted(
            @Parameter(
                description = "확인할 쿠폰 타입 (지정하지 않으면 전체 확인)", 
                required = false,
                example = "ONE_DAY"
            )
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
    @Operation(
        summary = "추첨 결과 초기화 (개발/테스트 용도)",
        description = "특정 쿠폰 타입 또는 전체 추첨 결과를 초기화합니다. 개발 및 테스트 환경에서만 사용해야 합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "추첨 결과 초기화 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "code": "SUCCESS",
                        "message": "추첨 결과가 성공적으로 초기화되었습니다",
                        "time": "2024-01-29T10:30:00",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetLottery(
            @Parameter(
                description = "초기화할 쿠폰 타입 (지정하지 않으면 전체 초기화)", 
                required = false,
                example = "ONE_DAY"
            )
            @RequestParam(required = false) CouponType couponType) {
        
        log.info("추첨 결과 초기화 요청 - 쿠폰 타입: {}", couponType != null ? couponType : "전체");
        
        ApiResponse<Void> response = lotteryService.resetLottery(couponType);
        
        log.info("추첨 결과 초기화 완료 - 쿠폰 타입: {}, 결과: {}", 
                couponType != null ? couponType : "전체", response.getCode());
        
        return ResponseEntity.ok(response);
    }
}