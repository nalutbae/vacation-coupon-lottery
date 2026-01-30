package com.kidaristudio.vacationcouponlottery.service.impl;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.LotteryResult;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.dto.CouponEntryStatus;
import com.kidaristudio.vacationcouponlottery.dto.UserCoinInfo;
import com.kidaristudio.vacationcouponlottery.dto.UserEntryStatus;
import com.kidaristudio.vacationcouponlottery.repository.LotteryResultRepository;
import com.kidaristudio.vacationcouponlottery.repository.SystemConfigRepository;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import com.kidaristudio.vacationcouponlottery.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 현황 조회 서비스 구현체
 * 개인/전체 응모 현황, 코인 현황 조회 기능을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatusServiceImpl implements StatusService {

    private final UserRepository userRepository;
    private final VacationCouponEntryRepository entryRepository;
    private final LotteryResultRepository lotteryResultRepository;
    private final SystemConfigRepository systemConfigRepository;

    @Override
    public ApiResponse<List<UserEntryStatus>> getUserEntryStatus(String phoneNumber) {
        log.debug("사용자 응모 현황 조회 시작: phoneNumber={}", phoneNumber);
        
        try {
            // 사용자 조회
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
            if (userOpt.isEmpty()) {
                log.debug("사용자를 찾을 수 없음: phoneNumber={}", phoneNumber);
                return ApiResponse.success(new ArrayList<>());
            }
            
            User user = userOpt.get();
            
            // 사용자의 모든 응모 내역 조회
            List<VacationCouponEntry> entries = entryRepository.findByUserOrderByCreatedAtDesc(user);
            
            // 당첨 정보 조회
            Map<Long, LotteryResult> winningResults = lotteryResultRepository.findWinningResultsByUserId(user.getId())
                    .stream()
                    .collect(Collectors.toMap(
                            result -> result.getEntry().getId(),
                            result -> result
                    ));
            
            // DTO 변환
            List<UserEntryStatus> statusList = entries.stream()
                    .map(entry -> {
                        LotteryResult winningResult = winningResults.get(entry.getId());
                        return UserEntryStatus.builder()
                                .entryId(entry.getId())
                                .couponType(entry.getCouponType())
                                .coinCount(entry.getCoinCount())
                                .isActive(entry.getIsActive())
                                .entryTime(entry.getCreatedAt())
                                .isWinner(winningResult != null)
                                .winningRank(winningResult != null ? winningResult.getRank() : null)
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.debug("사용자 응모 현황 조회 완료: phoneNumber={}, entryCount={}", phoneNumber, statusList.size());
            return ApiResponse.success(statusList);
            
        } catch (Exception e) {
            log.error("사용자 응모 현황 조회 중 오류 발생: phoneNumber={}", phoneNumber, e);
            return ApiResponse.error("QUERY_ERROR", "응모 현황 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public ApiResponse<Integer> getUserCoinCount(String phoneNumber) {
        log.debug("사용자 코인 수량 조회 시작: phoneNumber={}", phoneNumber);
        
        try {
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
            if (userOpt.isEmpty()) {
                log.debug("사용자를 찾을 수 없음: phoneNumber={}", phoneNumber);
                return ApiResponse.success(0);
            }
            
            Integer coinCount = userOpt.get().getCoinCount();
            log.debug("사용자 코인 수량 조회 완료: phoneNumber={}, coinCount={}", phoneNumber, coinCount);
            return ApiResponse.success(coinCount);
            
        } catch (Exception e) {
            log.error("사용자 코인 수량 조회 중 오류 발생: phoneNumber={}", phoneNumber, e);
            return ApiResponse.error("QUERY_ERROR", "코인 수량 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public ApiResponse<UserCoinInfo> getUserCoinInfo(String phoneNumber) {
        log.debug("사용자 코인 정보 조회 시작: phoneNumber={}", phoneNumber);
        
        try {
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
            if (userOpt.isEmpty()) {
                log.debug("사용자를 찾을 수 없음: phoneNumber={}", phoneNumber);
                UserCoinInfo defaultInfo = UserCoinInfo.builder()
                        .phoneNumber(phoneNumber)
                        .coinCount(0)
                        .totalAcquiredCoins(0)
                        .canAcquireMore(true)
                        .build();
                return ApiResponse.success(defaultInfo);
            }
            
            User user = userOpt.get();
            UserCoinInfo coinInfo = UserCoinInfo.builder()
                    .phoneNumber(phoneNumber)
                    .coinCount(user.getCoinCount())
                    .totalAcquiredCoins(user.getTotalAcquiredCoins())
                    .canAcquireMore(user.canAcquireMoreCoins())
                    .build();
            
            log.debug("사용자 코인 정보 조회 완료: phoneNumber={}, coinCount={}, totalAcquiredCoins={}", 
                    phoneNumber, user.getCoinCount(), user.getTotalAcquiredCoins());
            return ApiResponse.success(coinInfo);
            
        } catch (Exception e) {
            log.error("사용자 코인 정보 조회 중 오류 발생: phoneNumber={}", phoneNumber, e);
            return ApiResponse.error("QUERY_ERROR", "코인 정보 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public ApiResponse<List<CouponEntryStatus>> getAllEntryStatus() {
        log.debug("전체 응모 현황 조회 시작");
        
        try {
            // 쿠폰 타입별 활성 응모 수 집계
            Map<CouponType, Long> entryCounts = entryRepository.countActiveEntriesByCouponType();
            
            // 쿠폰 타입별 총 코인 수 집계
            Map<CouponType, Long> coinSums = entryRepository.sumCoinsByCouponType();
            
            // 쿠폰 타입별 당첨자 수 집계
            Map<CouponType, Long> winnerCounts = lotteryResultRepository.countWinnersByCouponType();
            
            // 각 쿠폰 타입별 상세 통계 계산
            List<CouponEntryStatus> statusList = new ArrayList<>();
            
            for (CouponType couponType : CouponType.values()) {
                Long entryCount = entryCounts.getOrDefault(couponType, 0L);
                Long totalCoins = coinSums.getOrDefault(couponType, 0L);
                Long winners = winnerCounts.getOrDefault(couponType, 0L);
                
                // 최대/최소 코인 수 계산
                List<VacationCouponEntry> typeEntries = entryRepository.findByCouponTypeAndIsActiveTrue(couponType);
                Integer maxCoins = typeEntries.stream()
                        .mapToInt(VacationCouponEntry::getCoinCount)
                        .max()
                        .orElse(0);
                Integer minCoins = typeEntries.stream()
                        .mapToInt(VacationCouponEntry::getCoinCount)
                        .min()
                        .orElse(0);
                
                // 평균 코인 수 계산
                Double averageCoins = entryCount > 0 ? (double) totalCoins / entryCount : 0.0;
                
                CouponEntryStatus status = CouponEntryStatus.builder()
                        .couponType(couponType)
                        .totalEntrants(entryCount)
                        .totalCoins(totalCoins)
                        .averageCoins(averageCoins)
                        .maxCoins(maxCoins)
                        .minCoins(minCoins)
                        .isLotteryCompleted(winners > 0)
                        .winnerCount(winners.intValue())
                        .build();
                
                statusList.add(status);
            }
            
            log.debug("전체 응모 현황 조회 완료: statusCount={}", statusList.size());
            return ApiResponse.success(statusList);
            
        } catch (Exception e) {
            log.error("전체 응모 현황 조회 중 오류 발생", e);
            return ApiResponse.error("QUERY_ERROR", "전체 응모 현황 조회 중 오류가 발생했습니다.");
        }
    }
    @Override
    public ApiResponse<CoinStatusResponse> getAllCoinStatus() {
        log.debug("전체 코인 현황 조회 시작");
        
        try {
            // 시스템 설정에서 코인 정보 조회
            int totalCoins = systemConfigRepository.getTotalCoins();
            int remainingCoins = systemConfigRepository.getCurrentRemainingCoins();
            int distributedCoins = totalCoins - remainingCoins;
            
            // 코인을 보유한 사용자 수 조회
            Long usersWithCoins = userRepository.countUsersWithCoins();
            
            // 최대 코인을 보유한 사용자 수 조회
            List<User> usersWithMaxCoins = userRepository.findUsersWithMaxCoins();
            Long maxCoinUserCount = (long) usersWithMaxCoins.size();
            
            // 사용자별 코인 현황 조회 (코인을 보유한 사용자만)
            List<User> allUsersWithCoins = userRepository.findUsersByCoinCountBetween(1, 3);
            List<CoinStatusResponse.UserCoinStatus> userCoinStatuses = allUsersWithCoins.stream()
                    .map(user -> CoinStatusResponse.UserCoinStatus.builder()
                            .phoneNumber(user.getPhoneNumber())
                            .coinCount(user.getCoinCount())
                            .build())
                    .collect(Collectors.toList());
            
            CoinStatusResponse response = CoinStatusResponse.builder()
                    .totalCoins(totalCoins)
                    .remainingCoins(remainingCoins)
                    .distributedCoins(distributedCoins)
                    .usersWithCoins(usersWithCoins)
                    .usersWithMaxCoins(maxCoinUserCount)
                    .userCoinStatuses(userCoinStatuses)
                    .build();
            
            log.debug("전체 코인 현황 조회 완료: totalCoins={}, remainingCoins={}, distributedCoins={}", 
                    totalCoins, remainingCoins, distributedCoins);
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("전체 코인 현황 조회 중 오류 발생", e);
            return ApiResponse.error("QUERY_ERROR", "전체 코인 현황 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public ApiResponse<SystemStatistics> getSystemStatistics() {
        log.debug("시스템 전체 통계 조회 시작");
        
        try {
            // 전체 사용자 수 조회
            Long totalUsers = userRepository.count();
            
            // 전체 활성 응모 수 조회
            Long totalEntries = entryRepository.countActiveEntries();
            
            // 분배된 코인 수 조회
            Long totalCoinsDistributed = userRepository.getTotalCoinCount();
            
            // 남은 코인 수 조회
            Integer remainingCoins = systemConfigRepository.getCurrentRemainingCoins();
            
            // 추첨 완료 여부 확인 (모든 쿠폰 타입에 대해 당첨자가 있는지 확인)
            List<CouponType> completedLotteries = lotteryResultRepository.findCompletedLotteryCouponTypes();
            Boolean isLotteryCompleted = completedLotteries.size() == CouponType.values().length;
            
            SystemStatistics statistics = new SystemStatistics(
                    totalUsers.intValue(),
                    totalEntries.intValue(),
                    totalCoinsDistributed.intValue(),
                    remainingCoins,
                    isLotteryCompleted
            );
            
            log.debug("시스템 전체 통계 조회 완료: totalUsers={}, totalEntries={}, distributedCoins={}, remainingCoins={}, lotteryCompleted={}", 
                    totalUsers, totalEntries, totalCoinsDistributed, remainingCoins, isLotteryCompleted);
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("시스템 전체 통계 조회 중 오류 발생", e);
            return ApiResponse.error("QUERY_ERROR", "시스템 통계 조회 중 오류가 발생했습니다.");
        }
    }
}