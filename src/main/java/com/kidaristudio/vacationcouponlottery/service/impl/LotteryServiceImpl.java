package com.kidaristudio.vacationcouponlottery.service.impl;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.LotteryResult;
import com.kidaristudio.vacationcouponlottery.dto.Winner;
import com.kidaristudio.vacationcouponlottery.exception.LotteryException;
import com.kidaristudio.vacationcouponlottery.repository.LotteryResultRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import com.kidaristudio.vacationcouponlottery.service.FairLotteryAlgorithm;
import com.kidaristudio.vacationcouponlottery.service.LotteryService;
import com.kidaristudio.vacationcouponlottery.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 추첨 서비스 구현체
 * 휴가 쿠폰 추첨 실행, 당첨자 조회, 결과 저장 로직을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotteryServiceImpl implements LotteryService {

    private final VacationCouponEntryRepository entryRepository;
    private final LotteryResultRepository lotteryResultRepository;
    private final FairLotteryAlgorithm fairLotteryAlgorithm;
    private final MessageService messageService;

    /**
     * 쿠폰당 당첨자 수 (요구사항에 따라 3명)
     */
    private static final int WINNERS_PER_COUPON = 3;

    @Override
    @Transactional
    public ApiResponse<LotteryResult> conductLottery(CouponType couponType) {
        log.info("추첨 실행 시작: couponType={}", couponType);

        try {
            // 1. 입력 값 검증
            validateLotteryRequest(couponType);

            // 2. 이미 추첨이 완료되었는지 확인
            if (isLotteryAlreadyCompleted(couponType)) {
                throw new LotteryException.LotteryAlreadyCompletedException(messageService);
            }

            // 3. 해당 쿠폰 타입의 활성 응모 내역 조회
            List<VacationCouponEntry> activeEntries = entryRepository.findActiveByCouponType(couponType);
            
            if (activeEntries.isEmpty()) {
                throw new LotteryException.NoEntrantsException(messageService);
            }

            log.info("추첨 대상 응모자: {}명, 총 응모 코인: {}개", 
                    activeEntries.size(), 
                    activeEntries.stream().mapToInt(VacationCouponEntry::getCoinCount).sum());

            // 4. 공정한 추첨 실행
            List<VacationCouponEntry> winners = fairLotteryAlgorithm.conductFairLottery(activeEntries, WINNERS_PER_COUPON);

            // 5. 추첨 결과 저장
            List<com.kidaristudio.vacationcouponlottery.domain.LotteryResult> savedResults = saveLotteryResults(winners, couponType);

            // 6. 결과 DTO 생성
            LotteryResult result = createLotteryResult(couponType, activeEntries, savedResults);

            log.info("추첨 실행 완료: couponType={}, 당첨자={}명", couponType, winners.size());

            return ApiResponse.success(messageService.getMessage("success.lottery.completed"), result);

        } catch (LotteryException.BaseLotteryException e) {
            log.warn("추첨 실행 실패: couponType={}, reason={}", couponType, e.getErrorMessage());
            throw e;
        } catch (Exception e) {
            log.error("추첨 실행 중 예상치 못한 오류 발생: couponType={}", couponType, e);
            throw new LotteryException.LotteryProcessingException(messageService);
        }
    }

    @Override
    @Transactional
    public ApiResponse<List<LotteryResult>> conductAllLotteries() {
        log.info("전체 추첨 실행 시작");

        try {
            List<LotteryResult> results = Arrays.stream(CouponType.values())
                    .map(couponType -> {
                        try {
                            return conductLottery(couponType).getData();
                        } catch (LotteryException.LotteryAlreadyCompletedException | 
                                 LotteryException.NoEntrantsException e) {
                            // 이미 완료되었거나 응모자가 없는 경우는 무시하고 계속 진행
                            log.info("쿠폰 {} 추첨 건너뜀: {}", couponType, e.getErrorMessage());
                            return null;
                        }
                    })
                    .filter(result -> result != null)
                    .collect(Collectors.toList());

            log.info("전체 추첨 실행 완료: 성공한 추첨={}개", results.size());

            return ApiResponse.success(messageService.getMessage("success.lottery.completed"), results);

        } catch (Exception e) {
            log.error("전체 추첨 실행 중 오류 발생", e);
            throw new LotteryException.LotteryProcessingException("전체 추첨 실행 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public ApiResponse<List<Winner>> getWinners(CouponType couponType) {
        log.debug("당첨자 조회: couponType={}", couponType);

        try {
            List<com.kidaristudio.vacationcouponlottery.domain.LotteryResult> lotteryResults;
            
            if (couponType != null) {
                lotteryResults = lotteryResultRepository.findByCouponTypeOrderByRank(couponType);
            } else {
                lotteryResults = lotteryResultRepository.findAllOrderByCouponTypeAndRank();
            }

            List<Winner> winners = lotteryResults.stream()
                    .map(this::convertToWinner)
                    .collect(Collectors.toList());

            log.debug("당첨자 조회 완료: couponType={}, 당첨자={}명", couponType, winners.size());

            return ApiResponse.success(winners);

        } catch (Exception e) {
            log.error("당첨자 조회 중 오류 발생: couponType={}", couponType, e);
            throw new LotteryException.LotteryProcessingException("당첨자 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public ApiResponse<Boolean> isLotteryCompleted(CouponType couponType) {
        log.debug("추첨 완료 여부 확인: couponType={}", couponType);

        try {
            boolean isCompleted;
            
            if (couponType != null) {
                isCompleted = isLotteryAlreadyCompleted(couponType);
            } else {
                // 모든 쿠폰 타입에 대해 추첨이 완료되었는지 확인
                isCompleted = Arrays.stream(CouponType.values())
                        .allMatch(this::isLotteryAlreadyCompleted);
            }

            return ApiResponse.success(isCompleted);

        } catch (Exception e) {
            log.error("추첨 완료 여부 확인 중 오류 발생: couponType={}", couponType, e);
            throw new LotteryException.LotteryProcessingException("추첨 완료 여부 확인 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> resetLottery(CouponType couponType) {
        log.info("추첨 결과 초기화: couponType={}", couponType);

        try {
            if (couponType != null) {
                lotteryResultRepository.deleteByCouponType(couponType);
                log.info("추첨 결과 초기화 완료: couponType={}", couponType);
            } else {
                lotteryResultRepository.deleteAll();
                log.info("전체 추첨 결과 초기화 완료");
            }

            return ApiResponse.success("추첨 결과가 초기화되었습니다.", null);

        } catch (Exception e) {
            log.error("추첨 결과 초기화 중 오류 발생: couponType={}", couponType, e);
            throw new LotteryException.LotteryProcessingException("추첨 결과 초기화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 추첨 요청 입력 값 검증
     */
    private void validateLotteryRequest(CouponType couponType) {
        if (couponType == null) {
            throw new LotteryException.InvalidLotteryRequestException(messageService);
        }
    }

    /**
     * 추첨이 이미 완료되었는지 확인
     */
    private boolean isLotteryAlreadyCompleted(CouponType couponType) {
        return lotteryResultRepository.existsByCouponType(couponType);
    }

    /**
     * 추첨 결과를 데이터베이스에 저장
     */
    private List<com.kidaristudio.vacationcouponlottery.domain.LotteryResult> saveLotteryResults(
            List<VacationCouponEntry> winners, CouponType couponType) {
        
        LocalDateTime lotteryTime = LocalDateTime.now();
        
        return winners.stream()
                .map(winner -> {
                    int rank = winners.indexOf(winner) + 1; // 1부터 시작하는 순위
                    
                    com.kidaristudio.vacationcouponlottery.domain.LotteryResult lotteryResult = 
                            com.kidaristudio.vacationcouponlottery.domain.LotteryResult.builder()
                                    .entry(winner)
                                    .couponType(couponType)
                                    .rank(rank)
                                    .lotteryDate(lotteryTime)
                                    .build();
                    
                    return lotteryResultRepository.save(lotteryResult);
                })
                .collect(Collectors.toList());
    }

    /**
     * 추첨 결과 DTO 생성
     */
    private LotteryResult createLotteryResult(CouponType couponType, 
                                            List<VacationCouponEntry> allEntries,
                                            List<com.kidaristudio.vacationcouponlottery.domain.LotteryResult> savedResults) {
        
        List<Winner> winners = savedResults.stream()
                .map(this::convertToWinner)
                .collect(Collectors.toList());

        int totalCoins = allEntries.stream().mapToInt(VacationCouponEntry::getCoinCount).sum();

        return LotteryResult.builder()
                .couponType(couponType)
                .totalEntrants(allEntries.size())
                .totalCoins(totalCoins)
                .winnerCount(winners.size())
                .winners(winners)
                .lotteryTime(LocalDateTime.now())
                .isSuccess(true)
                .message(String.format("%s 쿠폰 추첨이 완료되었습니다. 당첨자: %d명", 
                        couponType.getDisplayName(), winners.size()))
                .build();
    }

    /**
     * LotteryResult 엔티티를 Winner DTO로 변환
     */
    private Winner convertToWinner(com.kidaristudio.vacationcouponlottery.domain.LotteryResult lotteryResult) {
        VacationCouponEntry entry = lotteryResult.getEntry();
        
        return Winner.builder()
                .entryId(entry.getId())
                .phoneNumber(entry.getUser().getPhoneNumber())
                .couponType(entry.getCouponType())
                .coinCount(entry.getCoinCount())
                .rank(lotteryResult.getRank())
                .entryTime(entry.getCreatedAt())
                .winningTime(lotteryResult.getLotteryDate())
                .build();
    }
}