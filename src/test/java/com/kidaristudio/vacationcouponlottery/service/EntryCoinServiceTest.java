package com.kidaristudio.vacationcouponlottery.service;

import com.kidaristudio.vacationcouponlottery.domain.SystemConfig;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.dto.ApiResponse;
import com.kidaristudio.vacationcouponlottery.dto.CoinAcquisitionResult;
import com.kidaristudio.vacationcouponlottery.dto.CoinStatusResponse;
import com.kidaristudio.vacationcouponlottery.exception.CoinException;
import com.kidaristudio.vacationcouponlottery.repository.SystemConfigRepository;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.service.impl.EntryCoinServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * EntryCoinService 단위 테스트
 * 코인 관리 서비스의 정상 케이스, 한도 초과, 수량 부족 등 엣지 케이스를 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class EntryCoinServiceTest {

    @Autowired
    private EntryCoinService entryCoinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 시스템 설정 초기화
        initializeSystemConfig();
    }

    @Test
    @DisplayName("응모 코인 획득 - 정상 케이스")
    void acquireCoin_Success() {
        // Given: 유효한 사용자
        String phoneNumber = "010-1234-5678";

        // When: 코인 획득 요청
        ApiResponse<CoinAcquisitionResult> response = entryCoinService.acquireCoin(phoneNumber);

        // Then: 성공 응답 및 코인 증가 확인
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();
        
        CoinAcquisitionResult result = response.getData();
        assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(result.getCoinCount()).isEqualTo(1);
        assertThat(result.getTotalAcquiredCoins()).isEqualTo(1);
        assertThat(result.getAcquiredCoins()).isEqualTo(1);
        assertThat(result.getRemainingCoins()).isEqualTo(899);

        // 사용자 데이터 확인
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(1);
        assertThat(user.getTotalAcquiredCoins()).isEqualTo(1);
    }

    @Test
    @DisplayName("응모 코인 획득 - 연속 획득")
    void acquireCoin_MultipleAcquisitions() {
        // Given: 유효한 사용자
        String phoneNumber = "010-1234-5679";

        // When: 3번 연속 코인 획득
        ApiResponse<CoinAcquisitionResult> response1 = entryCoinService.acquireCoin(phoneNumber);
        ApiResponse<CoinAcquisitionResult> response2 = entryCoinService.acquireCoin(phoneNumber);
        ApiResponse<CoinAcquisitionResult> response3 = entryCoinService.acquireCoin(phoneNumber);

        // Then: 모든 요청 성공
        assertThat(response1.getCode()).isEqualTo("SUCCESS");
        assertThat(response2.getCode()).isEqualTo("SUCCESS");
        assertThat(response3.getCode()).isEqualTo("SUCCESS");

        // 최종 상태 확인
        assertThat(response3.getData().getCoinCount()).isEqualTo(3);
        assertThat(response3.getData().getTotalAcquiredCoins()).isEqualTo(3);
        assertThat(response3.getData().getRemainingCoins()).isEqualTo(897);

        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(3);
        assertThat(user.getTotalAcquiredCoins()).isEqualTo(3);
    }

    @Test
    @DisplayName("응모 코인 획득 - 한도 초과")
    void acquireCoin_ExceedsLimit() {
        // Given: 이미 누적 3개 코인을 획득한 사용자 (현재 보유는 3개)
        String phoneNumber = "010-1234-5680";
        createUserWithCoins(phoneNumber, 3, 3); // 보유 3개, 누적 3개

        // When & Then: 추가 획득 시도 시 예외 발생
        assertThatThrownBy(() -> entryCoinService.acquireCoin(phoneNumber))
            .isInstanceOf(CoinException.CoinLimitExceededException.class)
            .hasMessageContaining("응모 코인 한도를 초과했습니다. (최대 3개)");

        // 사용자의 코인 수는 변경되지 않아야 함
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(3);
        assertThat(user.getTotalAcquiredCoins()).isEqualTo(3);
    }

    @Test
    @DisplayName("응모 코인 획득 - 전체 수량 부족")
    void acquireCoin_NoRemainingCoins() {
        // Given: 전체 코인이 0개 남도록 설정
        String phoneNumber = "010-1234-5681";
        setRemainingCoins(0);

        // When & Then: 코인 획득 시도 시 예외 발생
        assertThatThrownBy(() -> entryCoinService.acquireCoin(phoneNumber))
            .isInstanceOf(CoinException.NoRemainingCoinsException.class)
            .hasMessageContaining("소진");

        // 전체 코인 수량은 변경되지 않아야 함
        int remainingCoins = systemConfigRepository.getCurrentRemainingCoins();
        assertThat(remainingCoins).isEqualTo(0);
    }

    @Test
    @DisplayName("사용자 코인 수량 조회 - 기존 사용자")
    void getUserCoinCount_ExistingUser() {
        // Given: 2개 코인을 보유한 사용자
        String phoneNumber = "010-1234-5682";
        createUserWithCoins(phoneNumber, 2);

        // When: 코인 수량 조회
        ApiResponse<Integer> response = entryCoinService.getUserCoinCount(phoneNumber);

        // Then: 정확한 수량 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자 코인 수량 조회 - 신규 사용자")
    void getUserCoinCount_NewUser() {
        // Given: 존재하지 않는 사용자
        String phoneNumber = "010-9999-9999";

        // When: 코인 수량 조회
        ApiResponse<Integer> response = entryCoinService.getUserCoinCount(phoneNumber);

        // Then: 0개 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 코인 현황 조회")
    void getAllCoinStatus() {
        // Given: 여러 사용자에게 코인 분배 (실제 획득 과정을 통해)
        entryCoinService.acquireCoin("010-1111-1111");
        entryCoinService.acquireCoin("010-1111-1111");
        entryCoinService.acquireCoin("010-1111-1111"); // 3개
        
        entryCoinService.acquireCoin("010-2222-2222");
        entryCoinService.acquireCoin("010-2222-2222"); // 2개
        
        entryCoinService.acquireCoin("010-3333-3333"); // 1개

        // When: 전체 현황 조회
        ApiResponse<CoinStatusResponse> response = entryCoinService.getAllCoinStatus();

        // Then: 정확한 현황 반환
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isNotNull();

        CoinStatusResponse status = response.getData();
        assertThat(status.getTotalCoins()).isEqualTo(900);
        assertThat(status.getDistributedCoins()).isEqualTo(6); // 3 + 2 + 1
        assertThat(status.getRemainingCoins()).isEqualTo(894); // 900 - 6
        assertThat(status.getUsersWithCoins()).isEqualTo(3);
        assertThat(status.getUsersWithMaxCoins()).isEqualTo(1);
        assertThat(status.getUserCoinStatuses()).hasSize(3);
    }

    @Test
    @DisplayName("코인 차감 - 정상 케이스")
    void deductCoins_Success() {
        // Given: 3개 코인을 보유한 사용자
        String phoneNumber = "010-1234-5683";
        createUserWithCoins(phoneNumber, 3);

        // When: 2개 코인 차감
        entryCoinService.deductCoins(phoneNumber, 2);

        // Then: 정확한 차감 확인
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("코인 차감 - 부족한 코인")
    void deductCoins_InsufficientCoins() {
        // Given: 1개 코인을 보유한 사용자
        String phoneNumber = "010-1234-5684";
        createUserWithCoins(phoneNumber, 1);

        // When & Then: 2개 차감 시도 시 예외 발생
        assertThatThrownBy(() -> entryCoinService.deductCoins(phoneNumber, 2))
            .isInstanceOf(CoinException.InsufficientCoinsException.class)
            .hasMessageContaining("부족");

        // 코인 수는 변경되지 않아야 함
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("코인 반환 - 정상 케이스")
    void returnCoins_Success() {
        // Given: 1개 코인을 보유한 사용자
        String phoneNumber = "010-1234-5685";
        createUserWithCoins(phoneNumber, 1);

        // When: 2개 코인 반환
        entryCoinService.returnCoins(phoneNumber, 2);

        // Then: 정확한 반환 확인
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("코인 반환 - 한도 초과")
    void returnCoins_ExceedsLimit() {
        // Given: 2개 코인을 보유한 사용자
        String phoneNumber = "010-1234-5686";
        createUserWithCoins(phoneNumber, 2);

        // When & Then: 2개 반환 시도 시 예외 발생 (총 4개가 되어 한도 초과)
        assertThatThrownBy(() -> entryCoinService.returnCoins(phoneNumber, 2))
            .isInstanceOf(CoinException.CoinLimitExceededException.class)
            .hasMessageContaining("한도를 초과");

        // 코인 수는 변경되지 않아야 함
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getCoinCount()).isEqualTo(2);
    }

    /**
     * 테스트용 사용자 생성 헬퍼 메서드
     */
    private User createUserWithCoins(String phoneNumber, int coinCount) {
        return createUserWithCoins(phoneNumber, coinCount, coinCount);
    }

    /**
     * 테스트용 사용자 생성 헬퍼 메서드 (누적 획득 코인 지정 가능)
     */
    private User createUserWithCoins(String phoneNumber, int coinCount, int totalAcquiredCoins) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> User.builder()
                        .phoneNumber(phoneNumber)
                        .coinCount(0)
                        .totalAcquiredCoins(0)
                        .build());
        
        // 코인 수량 직접 설정 (테스트용)
        User updatedUser = User.builder()
                .id(user.getId())
                .phoneNumber(phoneNumber)
                .coinCount(coinCount)
                .totalAcquiredCoins(totalAcquiredCoins)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        
        return userRepository.save(updatedUser);
    }

    /**
     * 전체 잔여 코인 설정 헬퍼 메서드
     */
    private void setRemainingCoins(int amount) {
        SystemConfig config = systemConfigRepository.findByConfigKey(SystemConfig.Keys.REMAINING_COINS)
                .orElseGet(() -> SystemConfig.builder()
                        .configKey(SystemConfig.Keys.REMAINING_COINS)
                        .configValue("900")
                        .description("남은 응모 코인 수량")
                        .build());
        
        config.updateValue(amount);
        systemConfigRepository.save(config);
    }

    /**
     * 시스템 설정 초기화 헬퍼 메서드
     */
    private void initializeSystemConfig() {
        // 전체 코인 수량 설정
        SystemConfig totalCoinsConfig = systemConfigRepository.findByConfigKey(SystemConfig.Keys.TOTAL_COINS)
                .orElseGet(() -> SystemConfig.builder()
                        .configKey(SystemConfig.Keys.TOTAL_COINS)
                        .configValue("900")
                        .description("전체 응모 코인 수량")
                        .build());
        systemConfigRepository.save(totalCoinsConfig);

        // 남은 코인 수량 설정
        SystemConfig remainingCoinsConfig = systemConfigRepository.findByConfigKey(SystemConfig.Keys.REMAINING_COINS)
                .orElseGet(() -> SystemConfig.builder()
                        .configKey(SystemConfig.Keys.REMAINING_COINS)
                        .configValue("900")
                        .description("남은 응모 코인 수량")
                        .build());
        systemConfigRepository.save(remainingCoinsConfig);

        // 사용자당 최대 코인 수 설정
        SystemConfig maxCoinsPerUserConfig = systemConfigRepository.findByConfigKey(SystemConfig.Keys.MAX_COINS_PER_USER)
                .orElseGet(() -> SystemConfig.builder()
                        .configKey(SystemConfig.Keys.MAX_COINS_PER_USER)
                        .configValue("3")
                        .description("사용자당 최대 코인 수")
                        .build());
        systemConfigRepository.save(maxCoinsPerUserConfig);

        // 쿠폰당 당첨자 수 설정
        SystemConfig winnersPerCouponConfig = systemConfigRepository.findByConfigKey(SystemConfig.Keys.WINNERS_PER_COUPON)
                .orElseGet(() -> SystemConfig.builder()
                        .configKey(SystemConfig.Keys.WINNERS_PER_COUPON)
                        .configValue("3")
                        .description("쿠폰당 당첨자 수")
                        .build());
        systemConfigRepository.save(winnersPerCouponConfig);
    }
}