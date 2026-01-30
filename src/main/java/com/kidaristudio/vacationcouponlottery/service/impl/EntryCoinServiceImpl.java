package com.kidaristudio.vacationcouponlottery.service.impl;

import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.exception.CoinException;
import com.kidaristudio.vacationcouponlottery.repository.SystemConfigRepository;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.service.EntryCoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 응모 코인 관리 서비스 구현체
 * 동시성 처리와 트랜잭션 관리를 통해 안전한 코인 분배를 보장합니다.
 * SERIALIZABLE 격리 수준을 사용하여 데이터 일관성을 유지합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryCoinServiceImpl implements EntryCoinService {

    private final UserRepository userRepository;
    private final SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ApiResponse<CoinAcquisitionResult> acquireCoin(String phoneNumber) {
        log.info("응모 코인 획득 요청: phoneNumber={}", phoneNumber);

        try {
            // 1. 전체 코인 잔여량 확인
            int remainingCoins = systemConfigRepository.getCurrentRemainingCoins();
            if (remainingCoins <= 0) {
                throw new CoinException.NoRemainingCoinsException();
            }

            // 2. 사용자 조회 또는 생성 (락 적용)
            User user = userRepository.findByPhoneNumberWithLock(phoneNumber)
                    .orElseGet(() -> createNewUser(phoneNumber));

            // 3. 사용자별 누적 코인 획득 한도 확인
            if (!user.canAcquireMoreCoins()) {
                throw new CoinException.CoinLimitExceededException("누적 응모 코인 획득 한도(3개)에 도달했습니다.");
            }

            // 4. 원자적 코인 분배
            boolean success = systemConfigRepository.decrementRemainingCoins(1);
            if (!success) {
                throw new CoinException.NoRemainingCoinsException("동시 요청으로 인해 코인이 소진되었습니다.");
            }

            // 5. 사용자 코인 획득 (누적 획득 수도 함께 증가)
            user.acquireCoin();
            userRepository.save(user);

            // 6. 결과 생성
            int newRemainingCoins = systemConfigRepository.getCurrentRemainingCoins();
            CoinAcquisitionResult result = CoinAcquisitionResult.builder()
                    .phoneNumber(phoneNumber)
                    .coinCount(user.getCoinCount())
                    .totalAcquiredCoins(user.getTotalAcquiredCoins())
                    .acquiredCoins(1)
                    .remainingCoins(newRemainingCoins)
                    .build();

            log.info("응모 코인 획득 성공: phoneNumber={}, coinCount={}, totalAcquiredCoins={}, remainingCoins={}", 
                    phoneNumber, user.getCoinCount(), user.getTotalAcquiredCoins(), newRemainingCoins);

            return ApiResponse.success("응모 코인을 성공적으로 획득했습니다.", result);

        } catch (CoinException.CoinLimitExceededException | CoinException.NoRemainingCoinsException e) {
            log.warn("응모 코인 획득 실패: phoneNumber={}, reason={}", phoneNumber, e.getErrorMessage());
            throw e;
        } catch (Exception e) {
            log.error("응모 코인 획득 중 예상치 못한 오류 발생: phoneNumber={}", phoneNumber, e);
            throw new CoinException.CoinAcquisitionFailedException("응모 코인 획득 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public ApiResponse<Integer> getUserCoinCount(String phoneNumber) {
        log.debug("사용자 코인 수량 조회: phoneNumber={}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElse(null);

        int coinCount = user != null ? user.getCoinCount() : 0;
        
        log.debug("사용자 코인 수량 조회 결과: phoneNumber={}, coinCount={}", phoneNumber, coinCount);
        return ApiResponse.success(coinCount);
    }

    @Override
    public ApiResponse<CoinStatusResponse> getAllCoinStatus() {
        log.debug("전체 코인 현황 조회 시작");

        // 시스템 설정 조회
        int totalCoins = systemConfigRepository.getTotalCoins();
        int remainingCoins = systemConfigRepository.getCurrentRemainingCoins();
        int distributedCoins = totalCoins - remainingCoins;

        // 사용자 통계 조회
        Long usersWithCoins = userRepository.countUsersWithCoins();
        Long usersWithMaxCoins = (long) userRepository.findUsersWithMaxCoins().size();

        // 사용자별 코인 현황 조회 (코인을 보유한 사용자만)
        List<User> usersWithCoinsList = userRepository.findUsersByCoinCountBetween(1, 3);
        List<CoinStatusResponse.UserCoinStatus> userCoinStatuses = usersWithCoinsList.stream()
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
                .usersWithMaxCoins(usersWithMaxCoins)
                .userCoinStatuses(userCoinStatuses)
                .build();

        log.debug("전체 코인 현황 조회 완료: totalCoins={}, remainingCoins={}, distributedCoins={}, usersWithCoins={}", 
                totalCoins, remainingCoins, distributedCoins, usersWithCoins);

        return ApiResponse.success(response);
    }

    @Override
    @Transactional
    public void deductCoins(String phoneNumber, int amount) {
        log.debug("사용자 코인 차감: phoneNumber={}, amount={}", phoneNumber, amount);

        User user = userRepository.findByPhoneNumberWithLock(phoneNumber)
                .orElseThrow(() -> new CoinException.InsufficientCoinsException("사용자를 찾을 수 없습니다."));

        if (!user.canEnterLottery(amount)) {
            throw new CoinException.InsufficientCoinsException(
                    String.format("보유 코인이 부족합니다. 보유: %d개, 필요: %d개", user.getCoinCount(), amount));
        }

        user.decreaseCoinCount(amount);
        userRepository.save(user);

        log.debug("사용자 코인 차감 완료: phoneNumber={}, remainingCoins={}", phoneNumber, user.getCoinCount());
    }

    @Override
    @Transactional
    public void returnCoins(String phoneNumber, int amount) {
        log.debug("사용자 코인 반환: phoneNumber={}, amount={}", phoneNumber, amount);

        User user = userRepository.findByPhoneNumberWithLock(phoneNumber)
                .orElseThrow(() -> new CoinException.CoinAcquisitionFailedException("사용자를 찾을 수 없습니다."));

        if (user.getCoinCount() + amount > 3) {
            throw new CoinException.CoinLimitExceededException(
                    String.format("코인 반환 후 한도를 초과합니다. 현재: %d개, 반환: %d개", user.getCoinCount(), amount));
        }

        user.increaseCoinCount(amount);
        userRepository.save(user);

        log.debug("사용자 코인 반환 완료: phoneNumber={}, totalCoins={}", phoneNumber, user.getCoinCount());
    }

    /**
     * 새 사용자 생성
     */
    private User createNewUser(String phoneNumber) {
        log.debug("새 사용자 생성: phoneNumber={}", phoneNumber);
        
        User newUser = User.builder()
                .phoneNumber(phoneNumber)
                .coinCount(0)
                .build();
        
        return userRepository.save(newUser);
    }
}