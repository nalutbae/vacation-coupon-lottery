package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.EntryResult;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("동시성 테스트")
class ConcurrencyTest {

    @Autowired
    private EntryCoinService entryCoinService;

    @Autowired
    private VacationCouponService vacationCouponService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationCouponEntryRepository entryRepository;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(20);
        // 테스트 데이터 초기화
        entryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("다중 사용자 동시 코인 획득 - 전체 코인 수량 제한 검증")
    void testConcurrentCoinAcquisitionWithGlobalLimit() throws InterruptedException {
        // Given: 전체 코인 수량을 10개로 제한
        int totalCoinLimit = 10;
        int numberOfUsers = 15; // 제한보다 많은 사용자
        int threadsPerUser = 3; // 각 사용자당 3번 시도 (누적 제한)
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfUsers * threadsPerUser);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        List<Future<ApiResponse<CoinAcquisitionResult>>> futures = new ArrayList<>();

        // When: 여러 사용자가 동시에 코인 획득 시도
        for (int i = 0; i < numberOfUsers; i++) {
            String phoneNumber = String.format("010-1234-%04d", i);
            
            for (int j = 0; j < threadsPerUser; j++) {
                Future<ApiResponse<CoinAcquisitionResult>> future = executorService.submit(() -> {
                    try {
                        startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
                        ApiResponse<CoinAcquisitionResult> result = entryCoinService.acquireCoin(phoneNumber);
                        if ("SUCCESS".equals(result.getCode())) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                        return result;
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        return ApiResponse.error("COIN_ACQUISITION_FAILED", "예외 발생: " + e.getMessage());
                    } finally {
                        endLatch.countDown();
                    }
                });
                futures.add(future);
            }
        }

        // 모든 스레드 동시 시작
        startLatch.countDown();
        
        // 모든 작업 완료 대기 (최대 30초)
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // Then: 전체 코인 수량 제한이 올바르게 작동해야 함
        assertThat(successCount.get()).isLessThanOrEqualTo(totalCoinLimit);
        assertThat(successCount.get() + failureCount.get()).isEqualTo(numberOfUsers * threadsPerUser);
        
        // 실제 데이터베이스에서 코인 수량 확인
        int totalCoinsInDb = userRepository.findAll().stream()
                .mapToInt(User::getCoinCount)
                .sum();
        assertThat(totalCoinsInDb).isEqualTo(successCount.get());
        
        System.out.println("성공한 코인 획득: " + successCount.get());
        System.out.println("실패한 코인 획득: " + failureCount.get());
        System.out.println("DB의 총 코인 수: " + totalCoinsInDb);
    }

    @Test
    @DisplayName("개인별 누적 코인 획득 제한 - 동시성 검증")
    void testConcurrentCoinAcquisitionWithPersonalLimit() throws InterruptedException {
        // Given: 한 사용자가 동시에 여러 번 코인 획득 시도
        String phoneNumber = "010-1234-5678";
        int concurrentAttempts = 10; // 10번 동시 시도
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentAttempts);
        
        AtomicInteger successCount = new AtomicInteger(0);
        List<Future<ApiResponse<CoinAcquisitionResult>>> futures = new ArrayList<>();

        // When: 한 사용자가 동시에 여러 번 코인 획득 시도
        for (int i = 0; i < concurrentAttempts; i++) {
            Future<ApiResponse<CoinAcquisitionResult>> future = executorService.submit(() -> {
                try {
                    startLatch.await();
                    ApiResponse<CoinAcquisitionResult> result = entryCoinService.acquireCoin(phoneNumber);
                    if ("SUCCESS".equals(result.getCode())) {
                        successCount.incrementAndGet();
                    }
                    return result;
                } catch (Exception e) {
                    return ApiResponse.error("COIN_ACQUISITION_FAILED", "예외 발생: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            futures.add(future);
        }

        startLatch.countDown();
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // Then: 개인별 최대 3개 제한이 올바르게 작동해야 함
        assertThat(successCount.get()).isLessThanOrEqualTo(3);
        
        // 데이터베이스에서 실제 사용자 정보 확인
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (user != null) {
            assertThat(user.getCoinCount()).isEqualTo(successCount.get());
            assertThat(user.getTotalAcquiredCoins()).isEqualTo(successCount.get());
            assertThat(user.getTotalAcquiredCoins()).isLessThanOrEqualTo(3);
        }
        
        System.out.println("동시 시도 중 성공한 코인 획득: " + successCount.get());
        if (user != null) {
            System.out.println("사용자 현재 코인: " + user.getCoinCount());
            System.out.println("사용자 누적 획득 코인: " + user.getTotalAcquiredCoins());
        }
    }

    @Test
    @DisplayName("동시 응모 요청 처리 - 중복 방지 검증")
    void testConcurrentVacationCouponEntry() throws InterruptedException {
        // Given: 사용자에게 코인 3개 부여
        String phoneNumber = "010-1234-9999";
        entryCoinService.acquireCoin(phoneNumber);
        entryCoinService.acquireCoin(phoneNumber);
        entryCoinService.acquireCoin(phoneNumber);
        
        int concurrentEntries = 5; // 5번 동시 응모 시도
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentEntries);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Future<ApiResponse<EntryResult>>> futures = new ArrayList<>();

        // When: 같은 사용자가 동시에 여러 번 응모 시도
        for (int i = 0; i < concurrentEntries; i++) {
            Future<ApiResponse<EntryResult>> future = executorService.submit(() -> {
                try {
                    startLatch.await();
                    ApiResponse<EntryResult> result = vacationCouponService.enterLottery(phoneNumber, CouponType.ONE_DAY, 1);
                    if ("SUCCESS".equals(result.getCode())) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    return result;
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    return ApiResponse.error("ENTRY_FAILED", "예외 발생: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            futures.add(future);
        }

        startLatch.countDown();
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // Then: 코인 수량만큼만 응모가 성공해야 함
        assertThat(successCount.get()).isLessThanOrEqualTo(3); // 최대 3개 코인
        assertThat(successCount.get() + failureCount.get()).isEqualTo(concurrentEntries);
        
        // 데이터베이스에서 실제 응모 내역 확인
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        
        List<VacationCouponEntry> entries = entryRepository.findByUserOrderByCreatedAtDesc(user);
        assertThat(entries).hasSize(successCount.get());
        
        // 남은 코인 수 확인
        int expectedRemainingCoins = 3 - successCount.get();
        assertThat(user.getCoinCount()).isEqualTo(expectedRemainingCoins);
        
        System.out.println("동시 응모 시도 중 성공: " + successCount.get());
        System.out.println("동시 응모 시도 중 실패: " + failureCount.get());
        System.out.println("실제 응모 내역 수: " + entries.size());
        System.out.println("사용자 남은 코인: " + user.getCoinCount());
    }

    @Test
    @DisplayName("다중 사용자 동시 응모 - 코인 차감 정확성 검증")
    void testMultiUserConcurrentEntry() throws InterruptedException {
        // Given: 여러 사용자에게 각각 코인 부여
        int numberOfUsers = 10;
        List<String> phoneNumbers = new ArrayList<>();
        
        for (int i = 0; i < numberOfUsers; i++) {
            String phoneNumber = String.format("010-2000-%04d", i);
            phoneNumbers.add(phoneNumber);
            // 각 사용자에게 2개씩 코인 부여
            entryCoinService.acquireCoin(phoneNumber);
            entryCoinService.acquireCoin(phoneNumber);
        }
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfUsers);
        
        AtomicInteger totalSuccessCount = new AtomicInteger(0);
        List<Future<ApiResponse<EntryResult>>> futures = new ArrayList<>();

        // When: 모든 사용자가 동시에 응모
        for (String phoneNumber : phoneNumbers) {
            Future<ApiResponse<EntryResult>> future = executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    ApiResponse<EntryResult> result = vacationCouponService.enterLottery(phoneNumber, CouponType.THREE_DAY, 2);
                    if ("SUCCESS".equals(result.getCode())) {
                        totalSuccessCount.incrementAndGet();
                    }
                    return result;
                } catch (Exception e) {
                    return ApiResponse.error("ENTRY_FAILED", "예외 발생: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            futures.add(future);
        }

        startLatch.countDown();
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // Then: 모든 사용자의 응모가 성공해야 하고, 코인이 정확히 차감되어야 함
        assertThat(totalSuccessCount.get()).isEqualTo(numberOfUsers);
        
        // 모든 사용자의 코인이 0이 되었는지 확인
        for (String phoneNumber : phoneNumbers) {
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            assertThat(user).isNotNull();
            assertThat(user.getCoinCount()).isEqualTo(0);
            
            List<VacationCouponEntry> entries = entryRepository.findByUserOrderByCreatedAtDesc(user);
            assertThat(entries).hasSize(1);
            assertThat(entries.get(0).getCoinCount()).isEqualTo(2);
        }
        
        System.out.println("다중 사용자 동시 응모 성공: " + totalSuccessCount.get());
        System.out.println("총 응모 내역 수: " + entryRepository.count());
    }
}