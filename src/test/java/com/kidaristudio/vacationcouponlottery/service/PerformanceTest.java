package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.SystemConfig;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.EntryResult;
import com.kidaristudio.vacationcouponlottery.dto.LotteryResult;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.SystemConfigRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("성능 테스트")
class PerformanceTest {

    @Autowired
    private EntryCoinService entryCoinService;

    @Autowired
    private VacationCouponService vacationCouponService;

    @Autowired
    private LotteryService lotteryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationCouponEntryRepository entryRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        entryRepository.deleteAll();
        userRepository.deleteAll();
        
        // 시스템 설정 초기화 (코인 수량 복원)
        SystemConfig totalCoinsConfig = systemConfigRepository.findByConfigKey("TOTAL_COINS")
                .orElse(SystemConfig.builder()
                        .configKey("TOTAL_COINS")
                        .configValue("10000")
                        .description("전체 코인 수량")
                        .build());
        totalCoinsConfig.updateValue("10000");
        systemConfigRepository.save(totalCoinsConfig);
    }

    @Test
    @DisplayName("대량 사용자 코인 획득 성능 테스트")
    void testBulkCoinAcquisitionPerformance() {
        // Given: 대량 사용자 (1000명)
        int numberOfUsers = 1000;
        List<String> phoneNumbers = new ArrayList<>();
        
        for (int i = 0; i < numberOfUsers; i++) {
            phoneNumbers.add(String.format("010-3000-%04d", i));
        }

        // When: 대량 코인 획득 성능 측정
        StopWatch stopWatch = new StopWatch("대량 코인 획득 성능 테스트");
        stopWatch.start("코인 획득");
        
        int successCount = 0;
        for (String phoneNumber : phoneNumbers) {
            ApiResponse<CoinAcquisitionResult> result = entryCoinService.acquireCoin(phoneNumber);
            if ("SUCCESS".equals(result.getCode())) {
                successCount++;
            }
        }
        
        stopWatch.stop();

        // Then: 성능 검증
        long totalTimeMs = stopWatch.getLastTaskTimeMillis();
        double avgTimePerUser = (double) totalTimeMs / numberOfUsers;
        
        System.out.println("=== 대량 코인 획득 성능 결과 ===");
        System.out.println("총 사용자 수: " + numberOfUsers);
        System.out.println("성공한 코인 획득: " + successCount);
        System.out.println("총 소요 시간: " + totalTimeMs + "ms");
        System.out.println("사용자당 평균 시간: " + String.format("%.2f", avgTimePerUser) + "ms");
        System.out.println("초당 처리량: " + String.format("%.2f", 1000.0 / avgTimePerUser) + " users/sec");
        
        // 성능 기준: 사용자당 평균 100ms 이하
        assertThat(avgTimePerUser).isLessThan(100.0);
        assertThat(successCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("대량 응모 처리 성능 테스트")
    void testBulkEntryPerformance() {
        // Given: 대량 사용자에게 코인 부여
        int numberOfUsers = 500;
        List<String> phoneNumbers = new ArrayList<>();
        
        // 사용자들에게 코인 부여
        for (int i = 0; i < numberOfUsers; i++) {
            String phoneNumber = String.format("010-4000-%04d", i);
            phoneNumbers.add(phoneNumber);
            entryCoinService.acquireCoin(phoneNumber);
            entryCoinService.acquireCoin(phoneNumber);
        }

        // When: 대량 응모 성능 측정
        StopWatch stopWatch = new StopWatch("대량 응모 성능 테스트");
        stopWatch.start("응모 처리");
        
        int successCount = 0;
        for (String phoneNumber : phoneNumbers) {
            ApiResponse<EntryResult> result = vacationCouponService.enterLottery(phoneNumber, CouponType.ONE_DAY, 2);
            if ("SUCCESS".equals(result.getCode())) {
                successCount++;
            }
        }
        
        stopWatch.stop();

        // Then: 성능 검증
        long totalTimeMs = stopWatch.getLastTaskTimeMillis();
        double avgTimePerEntry = (double) totalTimeMs / numberOfUsers;
        
        System.out.println("=== 대량 응모 성능 결과 ===");
        System.out.println("총 응모 수: " + numberOfUsers);
        System.out.println("성공한 응모: " + successCount);
        System.out.println("총 소요 시간: " + totalTimeMs + "ms");
        System.out.println("응모당 평균 시간: " + String.format("%.2f", avgTimePerEntry) + "ms");
        System.out.println("초당 처리량: " + String.format("%.2f", 1000.0 / avgTimePerEntry) + " entries/sec");
        
        // 성능 기준: 응모당 평균 150ms 이하
        assertThat(avgTimePerEntry).isLessThan(150.0);
        assertThat(successCount).isEqualTo(numberOfUsers);
    }

    @Test
    @DisplayName("추첨 알고리즘 성능 테스트")
    void testLotteryAlgorithmPerformance() {
        // Given: 대량 응모 데이터 생성
        int numberOfUsers = 1000;
        int entriesPerUser = 2;
        
        // 사용자들 생성 및 응모
        for (int i = 0; i < numberOfUsers; i++) {
            String phoneNumber = String.format("010-5000-%04d", i);
            
            // 각 사용자에게 3개 코인 부여
            entryCoinService.acquireCoin(phoneNumber);
            entryCoinService.acquireCoin(phoneNumber);
            entryCoinService.acquireCoin(phoneNumber);
            
            // 1일권에 2개 코인으로 응모
            vacationCouponService.enterLottery(phoneNumber, CouponType.ONE_DAY, entriesPerUser);
        }

        // When: 추첨 성능 측정
        StopWatch stopWatch = new StopWatch("추첨 알고리즘 성능 테스트");
        stopWatch.start("추첨 실행");
        
        // 1일권 추첨
        ApiResponse<LotteryResult> result = lotteryService.conductLottery(CouponType.ONE_DAY);
        
        stopWatch.stop();

        // Then: 성능 검증
        long totalTimeMs = stopWatch.getLastTaskTimeMillis();
        int totalEntries = numberOfUsers * entriesPerUser;
        double timePerEntry = (double) totalTimeMs / totalEntries;
        
        System.out.println("=== 추첨 알고리즘 성능 결과 ===");
        System.out.println("총 응모 내역 수: " + totalEntries);
        System.out.println("당첨자 수: " + ("SUCCESS".equals(result.getCode()) && result.getData() != null ? result.getData().getWinners().size() : 0));
        System.out.println("총 소요 시간: " + totalTimeMs + "ms");
        System.out.println("응모 내역당 처리 시간: " + String.format("%.4f", timePerEntry) + "ms");
        
        // 성능 기준: 총 추첨 시간 5초 이하
        assertThat(totalTimeMs).isLessThan(5000);
        assertThat(result.getCode()).isEqualTo("SUCCESS");
        
        if (result.getData() != null) {
            assertThat(result.getData().getWinners()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("대량 데이터 조회 성능 테스트")
    void testBulkDataQueryPerformance() {
        // Given: 대량 데이터 생성
        int numberOfUsers = 500;
        
        // 사용자 및 응모 데이터 생성
        for (int i = 0; i < numberOfUsers; i++) {
            String phoneNumber = String.format("010-6000-%04d", i);
            
            // 코인 획득 및 응모
            entryCoinService.acquireCoin(phoneNumber);
            entryCoinService.acquireCoin(phoneNumber);
            vacationCouponService.enterLottery(phoneNumber, CouponType.ONE_DAY, 1);
            vacationCouponService.enterLottery(phoneNumber, CouponType.THREE_DAY, 1);
        }

        // When: 대량 데이터 조회 성능 측정
        StopWatch stopWatch = new StopWatch("대량 데이터 조회 성능 테스트");
        
        // 전체 사용자 조회
        stopWatch.start("전체 사용자 조회");
        List<User> allUsers = userRepository.findAll();
        stopWatch.stop();
        
        // 전체 응모 내역 조회
        stopWatch.start("전체 응모 내역 조회");
        List<VacationCouponEntry> allEntries = entryRepository.findAll();
        stopWatch.stop();
        
        // 개별 사용자 응모 내역 조회 (100명 샘플)
        stopWatch.start("개별 사용자 응모 내역 조회");
        int sampleSize = Math.min(100, numberOfUsers);
        for (int i = 0; i < sampleSize; i++) {
            String phoneNumber = String.format("010-6000-%04d", i);
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            if (user != null) {
                entryRepository.findByUserOrderByCreatedAtDesc(user);
            }
        }
        stopWatch.stop();

        // Then: 성능 검증 및 결과 출력
        System.out.println("=== 대량 데이터 조회 성능 결과 ===");
        System.out.println("총 사용자 수: " + allUsers.size());
        System.out.println("총 응모 내역 수: " + allEntries.size());
        System.out.println();
        
        for (StopWatch.TaskInfo task : stopWatch.getTaskInfo()) {
            System.out.println(task.getTaskName() + ": " + task.getTimeMillis() + "ms");
        }
        
        System.out.println("총 조회 시간: " + stopWatch.getTotalTimeMillis() + "ms");
        
        // 성능 기준: 각 조회 작업이 1초 이하
        for (StopWatch.TaskInfo task : stopWatch.getTaskInfo()) {
            assertThat(task.getTimeMillis()).isLessThan(1000);
        }
        
        assertThat(allUsers).hasSize(numberOfUsers);
        assertThat(allEntries).hasSize(numberOfUsers * 2); // 각 사용자당 2개 응모
    }

    @Test
    @DisplayName("메모리 사용량 테스트")
    void testMemoryUsage() {
        // Given: 메모리 사용량 측정 준비
        Runtime runtime = Runtime.getRuntime();
        
        // 초기 메모리 상태
        System.gc(); // 가비지 컬렉션 실행
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When: 대량 데이터 처리
        int numberOfUsers = 1000;
        List<String> phoneNumbers = new ArrayList<>();
        
        for (int i = 0; i < numberOfUsers; i++) {
            String phoneNumber = String.format("010-7000-%04d", i);
            phoneNumbers.add(phoneNumber);
            
            // 코인 획득 및 응모
            entryCoinService.acquireCoin(phoneNumber);
            entryCoinService.acquireCoin(phoneNumber);
            vacationCouponService.enterLottery(phoneNumber, CouponType.ONE_DAY, 2);
        }
        
        // 최종 메모리 상태
        System.gc(); // 가비지 컬렉션 실행
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        // Then: 메모리 사용량 검증
        double memoryPerUser = (double) memoryUsed / numberOfUsers;
        
        System.out.println("=== 메모리 사용량 테스트 결과 ===");
        System.out.println("처리한 사용자 수: " + numberOfUsers);
        System.out.println("초기 메모리: " + String.format("%.2f", initialMemory / 1024.0 / 1024.0) + " MB");
        System.out.println("최종 메모리: " + String.format("%.2f", finalMemory / 1024.0 / 1024.0) + " MB");
        System.out.println("사용된 메모리: " + String.format("%.2f", memoryUsed / 1024.0 / 1024.0) + " MB");
        System.out.println("사용자당 메모리: " + String.format("%.2f", memoryPerUser / 1024.0) + " KB");
        
        // 메모리 사용량 기준: 사용자당 10KB 이하
        assertThat(memoryPerUser).isLessThan(10240); // 10KB
        
        // 전체 메모리 사용량이 100MB를 초과하지 않아야 함
        assertThat(memoryUsed).isLessThan(100 * 1024 * 1024); // 100MB
    }
}