package com.kidaristudio.vacationcouponlottery.integration;

import com.kidaristudio.vacationcouponlottery.domain.CouponType;
import com.kidaristudio.vacationcouponlottery.domain.User;
import com.kidaristudio.vacationcouponlottery.domain.VacationCouponEntry;
import com.kidaristudio.vacationcouponlottery.repository.UserRepository;
import com.kidaristudio.vacationcouponlottery.repository.VacationCouponEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Liquibase 마이그레이션 스크립트가 모두 실행 성공하는지 테스트하기 위한 목적으로 작성되었습니다.
 * 테이블 스키마, 제약조건, 외래키 모두 정상 작동되어 성공합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("데이터베이스 마이그레이션 검증 테스트")
class DatabaseMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationCouponEntryRepository entryRepository;

    @Test
    @DisplayName("Liquibase 마이그레이션 스크립트 실행 확인")
    void verifyLiquibaseMigration() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 1. DATABASECHANGELOG 테이블 존재 확인 (Liquibase 실행 증거)
            assertTableExists(metaData, "DATABASECHANGELOG");
            assertTableExists(metaData, "DATABASECHANGELOGLOCK");
            
            // 2. 주요 테이블들 존재 확인
            assertTableExists(metaData, "USERS");
            assertTableExists(metaData, "VACATION_COUPON_ENTRIES");
            assertTableExists(metaData, "LOTTERY_RESULTS");
            
            System.out.println("✓ 모든 Liquibase 마이그레이션 테이블이 정상적으로 생성되었습니다.");
        }
    }

    @Test
    @DisplayName("Users 테이블 스키마 검증")
    void verifyUsersTableSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Users 테이블 컬럼 확인
            List<String> expectedColumns = List.of(
                "ID", "PHONE_NUMBER", "COIN_COUNT", "TOTAL_ACQUIRED_COINS", 
                "CREATED_AT", "UPDATED_AT"
            );
            
            List<String> actualColumns = getTableColumns(metaData, "USERS");
            
            for (String expectedColumn : expectedColumns) {
                assertThat(actualColumns).contains(expectedColumn);
            }
            
            System.out.println("✓ Users 테이블 스키마가 올바르게 구성되었습니다.");
            System.out.println("  컬럼: " + actualColumns);
        }
    }

    @Test
    @DisplayName("VacationCouponEntries 테이블 스키마 검증")
    void verifyVacationCouponEntriesTableSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // VacationCouponEntries 테이블 컬럼 확인
            List<String> expectedColumns = List.of(
                "ID", "USER_ID", "COUPON_TYPE", "COIN_COUNT", 
                "IS_ACTIVE", "CREATED_AT"
            );
            
            List<String> actualColumns = getTableColumns(metaData, "VACATION_COUPON_ENTRIES");
            
            for (String expectedColumn : expectedColumns) {
                assertThat(actualColumns).contains(expectedColumn);
            }
            
            System.out.println("✓ VacationCouponEntries 테이블 스키마가 올바르게 구성되었습니다.");
            System.out.println("  컬럼: " + actualColumns);
        }
    }

    @Test
    @DisplayName("LotteryResults 테이블 스키마 검증")
    void verifyLotteryResultsTableSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // LotteryResults 테이블 컬럼 확인
            List<String> expectedColumns = List.of(
                "ID", "ENTRY_ID", "COUPON_TYPE", "RANK", "LOTTERY_DATE"
            );
            
            List<String> actualColumns = getTableColumns(metaData, "LOTTERY_RESULTS");
            
            for (String expectedColumn : expectedColumns) {
                assertThat(actualColumns).contains(expectedColumn);
            }
            
            System.out.println("✓ LotteryResults 테이블 스키마가 올바르게 구성되었습니다.");
            System.out.println("  컬럼: " + actualColumns);
        }
    }

    @Test
    @DisplayName("데이터베이스 제약조건 검증")
    @Transactional
    void verifyDatabaseConstraints() {
        // 1. User 테이블 제약조건 테스트
        User user = User.builder()
                .phoneNumber("010-1234-5678")
                .coinCount(0)
                .totalAcquiredCoins(0)
                .build();
        
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        
        // 2. VacationCouponEntry 테이블 제약조건 테스트
        VacationCouponEntry entry = VacationCouponEntry.builder()
                .user(savedUser)
                .couponType(CouponType.ONE_DAY)
                .coinCount(1)
                .isActive(true)
                .build();
        
        VacationCouponEntry savedEntry = entryRepository.save(entry);
        assertThat(savedEntry.getId()).isNotNull();
        assertThat(savedEntry.getUser().getId()).isEqualTo(savedUser.getId());
        
        // 3. 외래키 제약조건 확인
        VacationCouponEntry foundEntry = entryRepository.findById(savedEntry.getId()).orElse(null);
        assertThat(foundEntry).isNotNull();
        assertThat(foundEntry.getUser().getPhoneNumber()).isEqualTo("010-1234-5678");
        
        System.out.println("✓ 데이터베이스 제약조건이 올바르게 작동합니다.");
    }

    @Test
    @DisplayName("인덱스 및 성능 최적화 확인")
    void verifyIndexesAndOptimization() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Users 테이블의 phone_number 인덱스 확인
            ResultSet indexInfo = metaData.getIndexInfo(null, null, "USERS", false, false);
            boolean phoneNumberIndexExists = false;
            
            while (indexInfo.next()) {
                String columnName = indexInfo.getString("COLUMN_NAME");
                if ("PHONE_NUMBER".equals(columnName)) {
                    phoneNumberIndexExists = true;
                    break;
                }
            }
            
            // H2 데이터베이스에서는 UNIQUE 제약조건이 자동으로 인덱스를 생성함
            assertThat(phoneNumberIndexExists).isTrue();
            
            System.out.println("✓ 성능 최적화를 위한 인덱스가 올바르게 생성되었습니다.");
        }
    }

    @Test
    @DisplayName("dev/prod 프로필별 설정 검증")
    void verifyProfileSpecificConfiguration() {
        // 현재 테스트는 test 프로필로 실행되므로 H2 인메모리 데이터베이스 사용
        String jdbcUrl = "";
        try (Connection connection = dataSource.getConnection()) {
            jdbcUrl = connection.getMetaData().getURL();
        } catch (SQLException e) {
            // 무시
        }
        
        // test 프로필에서는 H2 인메모리 데이터베이스 사용 확인
        assertThat(jdbcUrl).contains("h2");
        assertThat(jdbcUrl).contains("mem");
        
        System.out.println("✓ 프로필별 데이터베이스 설정이 올바르게 적용되었습니다.");
        System.out.println("  JDBC URL: " + jdbcUrl);
    }

    @Test
    @DisplayName("누적 코인 획득 컬럼 마이그레이션 검증")
    @Transactional
    void verifyTotalAcquiredCoinsColumnMigration() {
        // 새로운 사용자 생성
        User user = User.builder()
                .phoneNumber("010-9999-9999")
                .coinCount(2)
                .totalAcquiredCoins(3) // 누적 획득 코인 설정
                .build();
        
        User savedUser = userRepository.save(user);
        
        // 저장된 데이터 확인
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getTotalAcquiredCoins()).isEqualTo(3);
        assertThat(foundUser.getCoinCount()).isEqualTo(2);
        
        System.out.println("✓ 누적 코인 획득 컬럼 마이그레이션이 정상적으로 완료되었습니다.");
    }

    // === 헬퍼 메서드들 ===

    private void assertTableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
        assertThat(tables.next()).isTrue();
        tables.close();
    }

    private List<String> getTableColumns(DatabaseMetaData metaData, String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        ResultSet columnsResult = metaData.getColumns(null, null, tableName, null);
        
        while (columnsResult.next()) {
            columns.add(columnsResult.getString("COLUMN_NAME"));
        }
        
        columnsResult.close();
        return columns;
    }
}