--liquibase formatted sql

--changeset vacation-coupon-lottery-system:02-create-vacation-coupon-entries-table
--comment: 휴가 쿠폰 응모 테이블 생성 - 사용자의 1일권/3일권 응모 내역

CREATE TABLE vacation_coupon_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_type VARCHAR(20) NOT NULL,
    coin_count INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vacation_coupon_entries_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_vacation_coupon_entries_coupon_type CHECK (coupon_type IN ('ONE_DAY', 'THREE_DAY')),
    CONSTRAINT chk_vacation_coupon_entries_coin_count CHECK (coin_count > 0)
);

CREATE INDEX idx_vacation_coupon_entries_user_id ON vacation_coupon_entries(user_id);
CREATE INDEX idx_vacation_coupon_entries_coupon_type ON vacation_coupon_entries(coupon_type);
CREATE INDEX idx_vacation_coupon_entries_is_active ON vacation_coupon_entries(is_active);

--rollback DROP TABLE vacation_coupon_entries;