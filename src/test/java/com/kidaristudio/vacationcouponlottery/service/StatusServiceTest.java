package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.LotteryResult;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.dto.CouponEntryStatus;
import com.kidaristudio.vacationcouponlottery.dto.UserEntryStatus;
import com.kidaristudio.vacationcouponlottery.repository.LotteryResultRepository;
import com.kidaristudio.vacationcouponlottery.repository.SystemConfigRepository;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import com.kidaristudio.vacationcouponlottery.service.impl.StatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * StatusService 단위 테스트
 * 현황 조회 서비스의 모든 기능을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StatusService 단위 테스트")
class StatusServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VacationCouponEntryRepository entryRepository;

    @Mock
    private LotteryResultRepository lotteryResultRepository;

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private StatusServiceImpl statusService;

    private User testUser;
    private VacationCouponEntry testEntry1;
    private VacationCouponEntry testEntry2;
    private LotteryResult testLotteryResult;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .id(1L)
                .phoneNumber("010-1234-5678")
                .coinCount(2)
                .build();

        // 테스트 응모 내역 생성
        testEntry1 = VacationCouponEntry.builder()
                .id(1L)
                .user(testUser)
                .couponType(CouponType.ONE_DAY)
                .coinCount(1)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        testEntry2 = VacationCouponEntry.builder()
                .id(2L)
                .user(testUser)
                .couponType(CouponType.THREE_DAY)
                .coinCount(1)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        // 테스트 추첨 결과 생성
        testLotteryResult = LotteryResult.builder()
                .id(1L)
                .entry(testEntry1)
                .couponType(CouponType.ONE_DAY)
                .rank(1)
                .lotteryDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("사용자 응모 현황 조회 - 성공")
    void getUserEntryStatus_Success() {
        // Given
        String phoneNumber = "010-1234-5678";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(entryRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(Arrays.asList(testEntry2, testEntry1)); // 최신순
        when(lotteryResultRepository.findWinningResultsByUserId(testUser.getId()))
                .thenReturn(Arrays.asList(testLotteryResult));

        // When
        ApiResponse<List<UserEntryStatus>> response = statusService.getUserEntryStatus(phoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(2);
        
        UserEntryStatus firstEntry = response.getData().get(0);
        assertThat(firstEntry.getEntryId()).isEqualTo(2L);
        assertThat(firstEntry.getCouponType()).isEqualTo(CouponType.THREE_DAY);
        assertThat(firstEntry.getIsWinner()).isFalse();
        
        UserEntryStatus secondEntry = response.getData().get(1);
        assertThat(secondEntry.getEntryId()).isEqualTo(1L);
        assertThat(secondEntry.getCouponType()).isEqualTo(CouponType.ONE_DAY);
        assertThat(secondEntry.getIsWinner()).isTrue();
        assertThat(secondEntry.getWinningRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("사용자 응모 현황 조회 - 사용자 없음")
    void getUserEntryStatus_UserNotFound() {
        // Given
        String phoneNumber = "010-9999-9999";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // When
        ApiResponse<List<UserEntryStatus>> response = statusService.getUserEntryStatus(phoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("사용자 코인 수량 조회 - 성공")
    void getUserCoinCount_Success() {
        // Given
        String phoneNumber = "010-1234-5678";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));

        // When
        ApiResponse<Integer> response = statusService.getUserCoinCount(phoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자 코인 수량 조회 - 사용자 없음")
    void getUserCoinCount_UserNotFound() {
        // Given
        String phoneNumber = "010-9999-9999";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // When
        ApiResponse<Integer> response = statusService.getUserCoinCount(phoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 응모 현황 조회 - 성공")
    void getAllEntryStatus_Success() {
        // Given
        Map<CouponType, Long> entryCounts = new HashMap<>();
        entryCounts.put(CouponType.ONE_DAY, 10L);
        entryCounts.put(CouponType.THREE_DAY, 8L);
        
        Map<CouponType, Long> coinSums = new HashMap<>();
        coinSums.put(CouponType.ONE_DAY, 15L);
        coinSums.put(CouponType.THREE_DAY, 12L);
        
        Map<CouponType, Long> winnerCounts = new HashMap<>();
        winnerCounts.put(CouponType.ONE_DAY, 3L);
        winnerCounts.put(CouponType.THREE_DAY, 0L);

        when(entryRepository.countActiveEntriesByCouponType()).thenReturn(entryCounts);
        when(entryRepository.sumCoinsByCouponType()).thenReturn(coinSums);
        when(lotteryResultRepository.countWinnersByCouponType()).thenReturn(winnerCounts);
        
        // 각 쿠폰 타입별 응모 내역 (최대/최소 코인 계산용)
        List<VacationCouponEntry> oneDayEntries = Arrays.asList(
                createEntry(CouponType.ONE_DAY, 1),
                createEntry(CouponType.ONE_DAY, 2),
                createEntry(CouponType.ONE_DAY, 1)
        );
        List<VacationCouponEntry> threeDayEntries = Arrays.asList(
                createEntry(CouponType.THREE_DAY, 2),
                createEntry(CouponType.THREE_DAY, 1)
        );
        
        when(entryRepository.findByCouponTypeAndIsActiveTrue(CouponType.ONE_DAY))
                .thenReturn(oneDayEntries);
        when(entryRepository.findByCouponTypeAndIsActiveTrue(CouponType.THREE_DAY))
                .thenReturn(threeDayEntries);

        // When
        ApiResponse<List<CouponEntryStatus>> response = statusService.getAllEntryStatus();

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(2);
        
        CouponEntryStatus oneDayStatus = response.getData().stream()
                .filter(status -> status.getCouponType() == CouponType.ONE_DAY)
                .findFirst().orElseThrow();
        
        assertThat(oneDayStatus.getTotalEntrants()).isEqualTo(10L);
        assertThat(oneDayStatus.getTotalCoins()).isEqualTo(15L);
        assertThat(oneDayStatus.getAverageCoins()).isEqualTo(1.5);
        assertThat(oneDayStatus.getMaxCoins()).isEqualTo(2);
        assertThat(oneDayStatus.getMinCoins()).isEqualTo(1);
        assertThat(oneDayStatus.getIsLotteryCompleted()).isTrue();
        assertThat(oneDayStatus.getWinnerCount()).isEqualTo(3);
    }
    @Test
    @DisplayName("전체 코인 현황 조회 - 성공")
    void getAllCoinStatus_Success() {
        // Given
        when(systemConfigRepository.getTotalCoins()).thenReturn(900);
        when(systemConfigRepository.getCurrentRemainingCoins()).thenReturn(700);
        when(userRepository.countUsersWithCoins()).thenReturn(50L);
        
        List<User> maxCoinUsers = Arrays.asList(
                createUser("010-1111-1111", 3),
                createUser("010-2222-2222", 3)
        );
        when(userRepository.findUsersWithMaxCoins()).thenReturn(maxCoinUsers);
        
        List<User> allUsersWithCoins = Arrays.asList(
                createUser("010-1111-1111", 3),
                createUser("010-2222-2222", 3),
                createUser("010-3333-3333", 2),
                createUser("010-4444-4444", 1)
        );
        when(userRepository.findUsersByCoinCountBetween(1, 3)).thenReturn(allUsersWithCoins);

        // When
        ApiResponse<CoinStatusResponse> response = statusService.getAllCoinStatus();

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        
        CoinStatusResponse data = response.getData();
        assertThat(data.getTotalCoins()).isEqualTo(900);
        assertThat(data.getRemainingCoins()).isEqualTo(700);
        assertThat(data.getDistributedCoins()).isEqualTo(200);
        assertThat(data.getUsersWithCoins()).isEqualTo(50L);
        assertThat(data.getUsersWithMaxCoins()).isEqualTo(2L);
        assertThat(data.getUserCoinStatuses()).hasSize(4);
        
        // 사용자별 코인 현황 검증
        CoinStatusResponse.UserCoinStatus firstUser = data.getUserCoinStatuses().get(0);
        assertThat(firstUser.getPhoneNumber()).isEqualTo("010-1111-1111");
        assertThat(firstUser.getCoinCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("시스템 전체 통계 조회 - 성공")
    void getSystemStatistics_Success() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(entryRepository.countActiveEntries()).thenReturn(150L);
        when(userRepository.getTotalCoinCount()).thenReturn(200L);
        when(systemConfigRepository.getCurrentRemainingCoins()).thenReturn(700);
        
        // 모든 쿠폰 타입에 대해 추첨 완료
        List<CouponType> completedLotteries = Arrays.asList(CouponType.ONE_DAY, CouponType.THREE_DAY);
        when(lotteryResultRepository.findCompletedLotteryCouponTypes()).thenReturn(completedLotteries);

        // When
        ApiResponse<StatusService.SystemStatistics> response = statusService.getSystemStatistics();

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        
        StatusService.SystemStatistics statistics = response.getData();
        assertThat(statistics.getTotalUsers()).isEqualTo(100);
        assertThat(statistics.getTotalEntries()).isEqualTo(150);
        assertThat(statistics.getTotalCoinsDistributed()).isEqualTo(200);
        assertThat(statistics.getRemainingCoins()).isEqualTo(700);
        assertThat(statistics.getIsLotteryCompleted()).isTrue();
    }

    @Test
    @DisplayName("시스템 전체 통계 조회 - 추첨 미완료")
    void getSystemStatistics_LotteryNotCompleted() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(entryRepository.countActiveEntries()).thenReturn(150L);
        when(userRepository.getTotalCoinCount()).thenReturn(200L);
        when(systemConfigRepository.getCurrentRemainingCoins()).thenReturn(700);
        
        // 일부 쿠폰 타입만 추첨 완료
        List<CouponType> completedLotteries = Arrays.asList(CouponType.ONE_DAY);
        when(lotteryResultRepository.findCompletedLotteryCouponTypes()).thenReturn(completedLotteries);

        // When
        ApiResponse<StatusService.SystemStatistics> response = statusService.getSystemStatistics();

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData().getIsLotteryCompleted()).isFalse();
    }

    @Test
    @DisplayName("전체 응모 현황 조회 - 응모 없음")
    void getAllEntryStatus_NoEntries() {
        // Given
        when(entryRepository.countActiveEntriesByCouponType()).thenReturn(new HashMap<>());
        when(entryRepository.sumCoinsByCouponType()).thenReturn(new HashMap<>());
        when(lotteryResultRepository.countWinnersByCouponType()).thenReturn(new HashMap<>());
        when(entryRepository.findByCouponTypeAndIsActiveTrue(any())).thenReturn(Arrays.asList());

        // When
        ApiResponse<List<CouponEntryStatus>> response = statusService.getAllEntryStatus();

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(2); // 모든 쿠폰 타입에 대해 상태 반환
        
        for (CouponEntryStatus status : response.getData()) {
            assertThat(status.getTotalEntrants()).isEqualTo(0L);
            assertThat(status.getTotalCoins()).isEqualTo(0L);
            assertThat(status.getAverageCoins()).isEqualTo(0.0);
            assertThat(status.getMaxCoins()).isEqualTo(0);
            assertThat(status.getMinCoins()).isEqualTo(0);
            assertThat(status.getIsLotteryCompleted()).isFalse();
            assertThat(status.getWinnerCount()).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("사용자 응모 현황 조회 - 예외 발생")
    void getUserEntryStatus_Exception() {
        // Given
        String phoneNumber = "010-1234-5678";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenThrow(new RuntimeException("Database error"));

        // When
        ApiResponse<List<UserEntryStatus>> response = statusService.getUserEntryStatus(phoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("QUERY_ERROR");
        assertThat(response.getMessage()).isEqualTo("응모 현황 조회 중 오류가 발생했습니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("사용자 코인 수량 조회 - 예외 발생")
    void getUserCoinCount_Exception() {
        // Given
        String phoneNumber = "010-1234-5678";
        when(userRepository.findByPhoneNumber(phoneNumber)).thenThrow(new RuntimeException("Database error"));

        // When
        ApiResponse<Integer> response = statusService.getUserCoinCount(phoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("QUERY_ERROR");
        assertThat(response.getMessage()).isEqualTo("코인 수량 조회 중 오류가 발생했습니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("전체 응모 현황 조회 - 예외 발생")
    void getAllEntryStatus_Exception() {
        // Given
        when(entryRepository.countActiveEntriesByCouponType()).thenThrow(new RuntimeException("Database error"));

        // When
        ApiResponse<List<CouponEntryStatus>> response = statusService.getAllEntryStatus();

        // Then
        assertThat(response.getCode()).isEqualTo("QUERY_ERROR");
        assertThat(response.getMessage()).isEqualTo("전체 응모 현황 조회 중 오류가 발생했습니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("전체 코인 현황 조회 - 예외 발생")
    void getAllCoinStatus_Exception() {
        // Given
        when(systemConfigRepository.getTotalCoins()).thenThrow(new RuntimeException("Database error"));

        // When
        ApiResponse<CoinStatusResponse> response = statusService.getAllCoinStatus();

        // Then
        assertThat(response.getCode()).isEqualTo("QUERY_ERROR");
        assertThat(response.getMessage()).isEqualTo("전체 코인 현황 조회 중 오류가 발생했습니다.");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("시스템 전체 통계 조회 - 예외 발생")
    void getSystemStatistics_Exception() {
        // Given
        when(userRepository.count()).thenThrow(new RuntimeException("Database error"));

        // When
        ApiResponse<StatusService.SystemStatistics> response = statusService.getSystemStatistics();

        // Then
        assertThat(response.getCode()).isEqualTo("QUERY_ERROR");
        assertThat(response.getMessage()).isEqualTo("시스템 통계 조회 중 오류가 발생했습니다.");
        assertThat(response.getData()).isNull();
    }

    // 헬퍼 메서드들
    private VacationCouponEntry createEntry(CouponType couponType, int coinCount) {
        return VacationCouponEntry.builder()
                .couponType(couponType)
                .coinCount(coinCount)
                .isActive(true)
                .build();
    }

    private User createUser(String phoneNumber, int coinCount) {
        return User.builder()
                .phoneNumber(phoneNumber)
                .coinCount(coinCount)
                .build();
    }
}