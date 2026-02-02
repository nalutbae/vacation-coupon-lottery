package com.kidaristudio.vacationcouponlottery.service.impl;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.*;
import com.kidaristudio.vacationcouponlottery.exception.CoinException;
import com.kidaristudio.vacationcouponlottery.exception.EntryException;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
import com.kidaristudio.vacationcouponlottery.service.MessageService;
import com.kidaristudio.vacationcouponlottery.service.VacationCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 휴가 쿠폰 응모 서비스 구현체
 * 응모 등록, 취소, 현황 조회 로직을 구현합니다.
 * 코인 차감 및 반환 처리를 포함합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VacationCouponServiceImpl implements VacationCouponService {

    private final VacationCouponEntryRepository entryRepository;
    private final UserRepository userRepository;
    private final EntryCoinService entryCoinService;
    private final MessageService messageService;

    @Override
    @Transactional
    public ApiResponse<EntryResult> enterLottery(String phoneNumber, CouponType couponType, int coinCount) {
        log.info("휴가 쿠폰 응모 요청: phoneNumber={}, couponType={}, coinCount={}", 
                phoneNumber, couponType, coinCount);

        try {
            // 1. 입력 값 검증
            validateEntryRequest(phoneNumber, couponType, coinCount);

            // 2. 사용자 조회 또는 생성
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new EntryException.UserNotFoundException(messageService));

            // 3. 사용자 코인 보유량 확인
            if (!user.canEnterLottery(coinCount)) {
                throw new CoinException.InsufficientCoinsException(messageService);
            }

            // 4. 코인 차감
            entryCoinService.deductCoins(phoneNumber, coinCount);

            // 5. 응모 등록
            VacationCouponEntry entry = VacationCouponEntry.builder()
                    .user(user)
                    .couponType(couponType)
                    .coinCount(coinCount)
                    .isActive(true)
                    .build();

            VacationCouponEntry savedEntry = entryRepository.save(entry);

            // 6. 사용자 정보 다시 조회 (코인 차감 후 상태)
            User updatedUser = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new EntryException.UserNotFoundException(messageService));

            // 7. 결과 생성
            EntryResult result = EntryResult.builder()
                    .entryId(savedEntry.getId())
                    .phoneNumber(phoneNumber)
                    .couponType(couponType)
                    .usedCoins(coinCount)
                    .remainingCoins(updatedUser.getCoinCount())
                    .entryTime(savedEntry.getCreatedAt())
                    .isActive(true)
                    .build();

            log.info("휴가 쿠폰 응모 성공: phoneNumber={}, entryId={}, couponType={}, usedCoins={}", 
                    phoneNumber, savedEntry.getId(), couponType, coinCount);

            return ApiResponse.success(messageService.getMessage("success.entry.registered"), result);

        } catch (CoinException.InsufficientCoinsException | EntryException.UserNotFoundException e) {
            log.warn("휴가 쿠폰 응모 실패: phoneNumber={}, reason={}", phoneNumber, e.getErrorMessage());
            throw e;
        } catch (Exception e) {
            log.error("휴가 쿠폰 응모 중 예상치 못한 오류 발생: phoneNumber={}", phoneNumber, e);
            throw new EntryException.EntryProcessingException(messageService);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CancelResult> cancelEntry(String phoneNumber, Long entryId) {
        log.info("휴가 쿠폰 응모 취소 요청: phoneNumber={}, entryId={}", phoneNumber, entryId);

        try {
            // 1. 입력 값 검증
            validateCancelRequest(phoneNumber, entryId);

            // 2. 응모 내역 조회
            VacationCouponEntry entry = entryRepository.findById(entryId)
                    .orElseThrow(() -> new EntryException.EntryNotFoundException(messageService));

            // 3. 응모자 본인 확인
            if (!entry.getUser().getPhoneNumber().equals(phoneNumber)) {
                throw new EntryException.UnauthorizedAccessException(messageService);
            }

            // 4. 응모 상태 확인
            if (!entry.getIsActive()) {
                throw new EntryException.AlreadyCancelledException(messageService);
            }

            // 5. 당첨 여부 확인 (당첨된 응모는 취소 불가)
            if (entry.isWinner()) {
                throw new EntryException.WinnerCannotCancelException(messageService);
            }

            // 6. 응모 취소
            entry.cancel();
            entryRepository.save(entry);

            // 7. 코인 반환
            entryCoinService.returnCoins(phoneNumber, entry.getCoinCount());

            // 8. 사용자 정보 다시 조회 (코인 반환 후 상태)
            User updatedUser = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new EntryException.UserNotFoundException(messageService));

            // 9. 결과 생성
            CancelResult result = CancelResult.builder()
                    .entryId(entryId)
                    .phoneNumber(phoneNumber)
                    .couponType(entry.getCouponType())
                    .returnedCoins(entry.getCoinCount())
                    .totalCoins(updatedUser.getCoinCount())
                    .cancelTime(LocalDateTime.now())
                    .build();

            log.info("휴가 쿠폰 응모 취소 성공: phoneNumber={}, entryId={}, returnedCoins={}", 
                    phoneNumber, entryId, entry.getCoinCount());

            return ApiResponse.success(messageService.getMessage("success.entry.cancelled"), result);

        } catch (EntryException.EntryNotFoundException | EntryException.UnauthorizedAccessException | 
                 EntryException.AlreadyCancelledException | EntryException.WinnerCannotCancelException e) {
            log.warn("휴가 쿠폰 응모 취소 실패: phoneNumber={}, entryId={}, reason={}", 
                    phoneNumber, entryId, e.getErrorMessage());
            throw e;
        } catch (Exception e) {
            log.error("휴가 쿠폰 응모 취소 중 예상치 못한 오류 발생: phoneNumber={}, entryId={}", 
                    phoneNumber, entryId, e);
            throw new EntryException.EntryProcessingException(messageService);
        }
    }

    @Override
    public ApiResponse<List<UserEntryStatus>> getUserEntries(String phoneNumber) {
        log.debug("사용자 응모 현황 조회: phoneNumber={}", phoneNumber);

        try {
            // 1. 사용자 조회
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElse(null);

            if (user == null) {
                // 사용자가 없으면 빈 목록 반환
                return ApiResponse.success("응모 내역이 없습니다.", List.of());
            }

            // 2. 사용자 응모 내역 조회
            List<VacationCouponEntry> entries = entryRepository.findByUserOrderByCreatedAtDesc(user);

            // 3. DTO 변환
            List<UserEntryStatus> entryStatuses = entries.stream()
                    .map(this::convertToUserEntryStatus)
                    .collect(Collectors.toList());

            log.debug("사용자 응모 현황 조회 완료: phoneNumber={}, entryCount={}", 
                    phoneNumber, entryStatuses.size());

            return ApiResponse.success(entryStatuses);

        } catch (Exception e) {
            log.error("사용자 응모 현황 조회 중 오류 발생: phoneNumber={}", phoneNumber, e);
            throw new EntryException.EntryProcessingException("응모 현황 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public ApiResponse<List<CouponEntryStatus>> getAllEntries() {
        log.debug("전체 응모 현황 조회 시작");

        try {
            // 1. 쿠폰 타입별 응모 통계 조회
            Map<CouponType, Long> entryCounts = entryRepository.countActiveEntriesByCouponType();
            Map<CouponType, Long> coinSums = entryRepository.sumCoinsByCouponType();
            Map<Integer, Long> coinCountStats = entryRepository.countEntriesByCoinCount();

            // 2. 각 쿠폰 타입별 상세 통계 생성
            List<CouponEntryStatus> entryStatuses = Arrays.stream(CouponType.values())
                    .map(couponType -> createCouponEntryStatus(couponType, entryCounts, coinSums, coinCountStats))
                    .collect(Collectors.toList());

            log.debug("전체 응모 현황 조회 완료: couponTypes={}", entryStatuses.size());

            return ApiResponse.success(entryStatuses);

        } catch (Exception e) {
            log.error("전체 응모 현황 조회 중 오류 발생", e);
            throw new EntryException.EntryProcessingException(messageService);
        }
    }

    /**
     * 응모 요청 입력 값 검증
     */
    private void validateEntryRequest(String phoneNumber, CouponType couponType, int coinCount) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new EntryException.InvalidRequestException(messageService);
        }
        if (couponType == null) {
            throw new EntryException.InvalidRequestException(messageService);
        }
        if (coinCount <= 0) {
            throw new EntryException.InvalidRequestException(messageService);
        }
        if (coinCount > 3) {
            throw new EntryException.InvalidRequestException(messageService);
        }
    }

    /**
     * 응모 취소 요청 입력 값 검증
     */
    private void validateCancelRequest(String phoneNumber, Long entryId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new EntryException.InvalidRequestException(messageService);
        }
        if (entryId == null || entryId <= 0) {
            throw new EntryException.InvalidRequestException(messageService);
        }
    }

    /**
     * VacationCouponEntry를 UserEntryStatus DTO로 변환
     */
    private UserEntryStatus convertToUserEntryStatus(VacationCouponEntry entry) {
        return UserEntryStatus.builder()
                .entryId(entry.getId())
                .couponType(entry.getCouponType())
                .coinCount(entry.getCoinCount())
                .isActive(entry.getIsActive())
                .entryTime(entry.getCreatedAt())
                .isWinner(entry.isWinner())
                .winningRank(entry.getWinningRank())
                .build();
    }

    /**
     * 쿠폰별 응모 현황 생성
     */
    private CouponEntryStatus createCouponEntryStatus(CouponType couponType, 
                                                    Map<CouponType, Long> entryCounts,
                                                    Map<CouponType, Long> coinSums,
                                                    Map<Integer, Long> coinCountStats) {
        Long totalEntrants = entryCounts.getOrDefault(couponType, 0L);
        Long totalCoins = coinSums.getOrDefault(couponType, 0L);

        // 평균, 최대, 최소 코인 수 계산
        Double averageCoins = totalEntrants > 0 ? (double) totalCoins / totalEntrants : 0.0;
        Integer maxCoins = coinCountStats.keySet().stream().max(Integer::compareTo).orElse(0);
        Integer minCoins = coinCountStats.keySet().stream().min(Integer::compareTo).orElse(0);

        return CouponEntryStatus.builder()
                .couponType(couponType)
                .totalEntrants(totalEntrants)
                .totalCoins(totalCoins)
                .averageCoins(averageCoins)
                .maxCoins(maxCoins)
                .minCoins(minCoins)
                .isLotteryCompleted(false) // 추첨 기능 구현 후 업데이트
                .winnerCount(0) // 추첨 기능 구현 후 업데이트
                .build();
    }
}