package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.dto.*;
import com.kidaristudio.vacationcouponlottery.exception.CoinException;
import com.kidaristudio.vacationcouponlottery.exception.EntryException;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * VacationCouponService 단위 테스트
 * 휴가 쿠폰 응모 서비스의 핵심 기능을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class VacationCouponServiceTest {

    @Autowired
    private VacationCouponService vacationCouponService;

    @Autowired
    private EntryCoinService entryCoinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationCouponEntryRepository entryRepository;

    private String testPhoneNumber;
    private User testUser;

    @BeforeEach
    void setUp() {
        testPhoneNumber = "010-1234-5678";
        
        // 테스트용 사용자 생성
        testUser = User.builder()
                .phoneNumber(testPhoneNumber)
                .coinCount(3) // 충분한 코인 제공
                .totalAcquiredCoins(3) // 누적 획득 코인도 설정
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 - 정상 케이스")
    void enterLottery_Success() {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        int coinCount = 2;

        // When
        ApiResponse<EntryResult> response = vacationCouponService.enterLottery(testPhoneNumber, couponType, coinCount);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();

        EntryResult result = response.getData();
        assertThat(result.getPhoneNumber()).isEqualTo(testPhoneNumber);
        assertThat(result.getCouponType()).isEqualTo(couponType);
        assertThat(result.getUsedCoins()).isEqualTo(coinCount);
        assertThat(result.getRemainingCoins()).isEqualTo(1); // 3 - 2 = 1
        assertThat(result.getIsActive()).isTrue();

        // 데이터베이스 확인
        User updatedUser = userRepository.findByPhoneNumber(testPhoneNumber).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCoinCount()).isEqualTo(1);

        List<VacationCouponEntry> entries = entryRepository.findByUserAndIsActiveTrue(updatedUser);
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getCouponType()).isEqualTo(couponType);
        assertThat(entries.get(0).getCoinCount()).isEqualTo(coinCount);
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 - 코인 부족")
    void enterLottery_InsufficientCoins() {
        // Given
        CouponType couponType = CouponType.THREE_DAY;
        int coinCount = 3; // 보유 코인과 같지만 실제로는 부족한 상황을 만들기 위해 사용자 코인을 1개로 설정
        
        // 사용자 코인을 1개로 설정
        testUser = User.builder()
                .id(testUser.getId())
                .phoneNumber(testPhoneNumber)
                .coinCount(1) // 1개만 보유
                .totalAcquiredCoins(testUser.getTotalAcquiredCoins()) // 누적은 유지
                .createdAt(testUser.getCreatedAt())
                .updatedAt(testUser.getUpdatedAt())
                .build();
        userRepository.save(testUser);

        // When & Then
        assertThatThrownBy(() -> vacationCouponService.enterLottery(testPhoneNumber, couponType, coinCount))
                .isInstanceOf(CoinException.InsufficientCoinsException.class)
                .hasMessageContaining("부족");

        // 사용자 상태 변경되지 않음 확인
        User unchangedUser = userRepository.findByPhoneNumber(testPhoneNumber).orElse(null);
        assertThat(unchangedUser).isNotNull();
        assertThat(unchangedUser.getCoinCount()).isEqualTo(1);

        List<VacationCouponEntry> entries = entryRepository.findByUserAndIsActiveTrue(unchangedUser);
        assertThat(entries).isEmpty();
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 - 잘못된 입력 (코인 수 초과)")
    void enterLottery_InvalidRequest_ExceedsMaxCoins() {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        int coinCount = 5; // 한 번에 최대 3개까지만 가능

        // When & Then
        assertThatThrownBy(() -> vacationCouponService.enterLottery(testPhoneNumber, couponType, coinCount))
                .isInstanceOf(EntryException.EntryProcessingException.class)
                .hasMessageContaining("오류가 발생했습니다");

        // 사용자 상태 변경되지 않음 확인
        User unchangedUser = userRepository.findByPhoneNumber(testPhoneNumber).orElse(null);
        assertThat(unchangedUser).isNotNull();
        assertThat(unchangedUser.getCoinCount()).isEqualTo(3);

        List<VacationCouponEntry> entries = entryRepository.findByUserAndIsActiveTrue(unchangedUser);
        assertThat(entries).isEmpty();
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 - 사용자 없음")
    void enterLottery_UserNotFound() {
        // Given
        String nonExistentPhoneNumber = "010-9999-9999";
        CouponType couponType = CouponType.ONE_DAY;
        int coinCount = 1;

        // When & Then
        assertThatThrownBy(() -> vacationCouponService.enterLottery(nonExistentPhoneNumber, couponType, coinCount))
                .isInstanceOf(EntryException.UserNotFoundException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 취소 - 정상 케이스")
    void cancelEntry_Success() {
        // Given: 먼저 응모 등록
        CouponType couponType = CouponType.ONE_DAY;
        int coinCount = 2;
        ApiResponse<EntryResult> entryResponse = vacationCouponService.enterLottery(testPhoneNumber, couponType, coinCount);
        Long entryId = entryResponse.getData().getEntryId();

        // When: 응모 취소
        ApiResponse<CancelResult> response = vacationCouponService.cancelEntry(testPhoneNumber, entryId);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();

        CancelResult result = response.getData();
        assertThat(result.getEntryId()).isEqualTo(entryId);
        assertThat(result.getPhoneNumber()).isEqualTo(testPhoneNumber);
        assertThat(result.getCouponType()).isEqualTo(couponType);
        assertThat(result.getReturnedCoins()).isEqualTo(coinCount);
        assertThat(result.getTotalCoins()).isEqualTo(3); // 원래 상태로 복원

        // 데이터베이스 확인
        User updatedUser = userRepository.findByPhoneNumber(testPhoneNumber).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCoinCount()).isEqualTo(3); // 코인 반환됨

        VacationCouponEntry entry = entryRepository.findById(entryId).orElse(null);
        assertThat(entry).isNotNull();
        assertThat(entry.getIsActive()).isFalse(); // 비활성화됨
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 취소 - 응모 내역 없음")
    void cancelEntry_EntryNotFound() {
        // Given
        Long nonExistentEntryId = 999L;

        // When & Then
        assertThatThrownBy(() -> vacationCouponService.cancelEntry(testPhoneNumber, nonExistentEntryId))
                .isInstanceOf(EntryException.EntryNotFoundException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 취소 - 타인의 응모")
    void cancelEntry_UnauthorizedAccess() {
        // Given: 첫 번째 사용자가 응모
        ApiResponse<EntryResult> entryResponse = vacationCouponService.enterLottery(testPhoneNumber, CouponType.ONE_DAY, 1);
        Long entryId = entryResponse.getData().getEntryId();

        // 두 번째 사용자
        String otherPhoneNumber = "010-9876-5432";

        // When & Then: 두 번째 사용자가 첫 번째 사용자의 응모 취소 시도
        assertThatThrownBy(() -> vacationCouponService.cancelEntry(otherPhoneNumber, entryId))
                .isInstanceOf(EntryException.UnauthorizedAccessException.class)
                .hasMessageContaining("본인");
    }

    @Test
    @DisplayName("휴가 쿠폰 응모 취소 - 이미 취소된 응모")
    void cancelEntry_AlreadyCancelled() {
        // Given: 응모 등록 후 취소
        ApiResponse<EntryResult> entryResponse = vacationCouponService.enterLottery(testPhoneNumber, CouponType.ONE_DAY, 1);
        Long entryId = entryResponse.getData().getEntryId();
        vacationCouponService.cancelEntry(testPhoneNumber, entryId);

        // When & Then: 이미 취소된 응모를 다시 취소 시도
        assertThatThrownBy(() -> vacationCouponService.cancelEntry(testPhoneNumber, entryId))
                .isInstanceOf(EntryException.AlreadyCancelledException.class)
                .hasMessageContaining("이미 취소");
    }

    @Test
    @DisplayName("사용자 응모 현황 조회 - 정상 케이스")
    void getUserEntries_Success() {
        // Given: 여러 응모 등록
        vacationCouponService.enterLottery(testPhoneNumber, CouponType.ONE_DAY, 1);
        vacationCouponService.enterLottery(testPhoneNumber, CouponType.THREE_DAY, 2);

        // When
        ApiResponse<List<UserEntryStatus>> response = vacationCouponService.getUserEntries(testPhoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();

        List<UserEntryStatus> entries = response.getData();
        assertThat(entries).hasSize(2);

        // 최신 순으로 정렬되어야 함
        assertThat(entries.get(0).getCouponType()).isEqualTo(CouponType.THREE_DAY);
        assertThat(entries.get(0).getCoinCount()).isEqualTo(2);
        assertThat(entries.get(0).getIsActive()).isTrue();

        assertThat(entries.get(1).getCouponType()).isEqualTo(CouponType.ONE_DAY);
        assertThat(entries.get(1).getCoinCount()).isEqualTo(1);
        assertThat(entries.get(1).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("사용자 응모 현황 조회 - 사용자 없음")
    void getUserEntries_UserNotFound() {
        // Given
        String nonExistentPhoneNumber = "010-9999-9999";

        // When
        ApiResponse<List<UserEntryStatus>> response = vacationCouponService.getUserEntries(nonExistentPhoneNumber);

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("전체 응모 현황 조회 - 정상 케이스")
    void getAllEntries_Success() {
        // Given: 여러 사용자의 응모 등록
        vacationCouponService.enterLottery(testPhoneNumber, CouponType.ONE_DAY, 1);
        vacationCouponService.enterLottery(testPhoneNumber, CouponType.THREE_DAY, 2);

        // 다른 사용자 생성 및 응모
        String otherPhoneNumber = "010-9876-5432";
        User otherUser = User.builder()
                .phoneNumber(otherPhoneNumber)
                .coinCount(3)
                .build();
        userRepository.save(otherUser);
        vacationCouponService.enterLottery(otherPhoneNumber, CouponType.ONE_DAY, 3);

        // When
        ApiResponse<List<CouponEntryStatus>> response = vacationCouponService.getAllEntries();

        // Then
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();

        List<CouponEntryStatus> entryStatuses = response.getData();
        assertThat(entryStatuses).hasSize(2); // ONE_DAY, THREE_DAY

        // ONE_DAY 쿠폰 확인
        CouponEntryStatus oneDayStatus = entryStatuses.stream()
                .filter(status -> status.getCouponType() == CouponType.ONE_DAY)
                .findFirst()
                .orElse(null);
        assertThat(oneDayStatus).isNotNull();
        assertThat(oneDayStatus.getTotalEntrants()).isEqualTo(2L); // 2명 응모
        assertThat(oneDayStatus.getTotalCoins()).isEqualTo(4L); // 1 + 3 = 4개 코인

        // THREE_DAY 쿠폰 확인
        CouponEntryStatus threeDayStatus = entryStatuses.stream()
                .filter(status -> status.getCouponType() == CouponType.THREE_DAY)
                .findFirst()
                .orElse(null);
        assertThat(threeDayStatus).isNotNull();
        assertThat(threeDayStatus.getTotalEntrants()).isEqualTo(1L); // 1명 응모
        assertThat(threeDayStatus.getTotalCoins()).isEqualTo(2L); // 2개 코인
    }

    @Test
    @DisplayName("쿠폰 타입별 자유 응모 - 정상 케이스")
    void enterLottery_MultipleCouponTypes() {
        // Given: 사용자에게 최대 코인 제공 (3개)
        // 이미 testUser는 3개의 코인을 가지고 있음

        // When: 서로 다른 쿠폰 타입에 응모
        ApiResponse<EntryResult> response1 = vacationCouponService.enterLottery(testPhoneNumber, CouponType.ONE_DAY, 1);
        ApiResponse<EntryResult> response2 = vacationCouponService.enterLottery(testPhoneNumber, CouponType.THREE_DAY, 2);

        // Then
        assertThat(response1.getCode()).isEqualTo("SUCCESS");
        assertThat(response2.getCode()).isEqualTo("SUCCESS");

        // 사용자 응모 현황 확인
        ApiResponse<List<UserEntryStatus>> statusResponse = vacationCouponService.getUserEntries(testPhoneNumber);
        List<UserEntryStatus> entries = statusResponse.getData();
        assertThat(entries).hasSize(2);

        // 두 쿠폰 타입 모두 응모되었는지 확인
        boolean hasOneDay = entries.stream().anyMatch(entry -> entry.getCouponType() == CouponType.ONE_DAY);
        boolean hasThreeDay = entries.stream().anyMatch(entry -> entry.getCouponType() == CouponType.THREE_DAY);
        assertThat(hasOneDay).isTrue();
        assertThat(hasThreeDay).isTrue();

        // 코인 차감 확인
        User updatedUser = userRepository.findByPhoneNumber(testPhoneNumber).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCoinCount()).isEqualTo(0); // 3 - 1 - 2 = 0
    }

    @Test
    @DisplayName("응모 처리 원자성 - 코인 차감과 응모 등록이 함께 처리됨")
    void enterLottery_Atomicity() {
        // Given
        CouponType couponType = CouponType.ONE_DAY;
        int coinCount = 3;
        int initialCoinCount = testUser.getCoinCount();
        int initialEntryCount = entryRepository.findByUserAndIsActiveTrue(testUser).size();

        // When
        ApiResponse<EntryResult> response = vacationCouponService.enterLottery(testPhoneNumber, couponType, coinCount);

        // Then: 원자적 처리 확인
        assertThat(response.getCode()).isEqualTo("SUCCESS");

        // 코인이 정확히 차감됨
        User updatedUser = userRepository.findByPhoneNumber(testPhoneNumber).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCoinCount()).isEqualTo(initialCoinCount - coinCount);

        // 응모 내역이 정확히 1개 증가
        List<VacationCouponEntry> entries = entryRepository.findByUserAndIsActiveTrue(updatedUser);
        assertThat(entries).hasSize(initialEntryCount + 1);

        // 응답 데이터 검증
        EntryResult result = response.getData();
        assertThat(result.getUsedCoins()).isEqualTo(coinCount);
        assertThat(result.getRemainingCoins()).isEqualTo(initialCoinCount - coinCount);
    }
}