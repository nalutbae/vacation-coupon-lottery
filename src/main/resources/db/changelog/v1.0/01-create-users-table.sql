--liquibase formatted sql

--changeset vacation-coupon-lottery-system:01-create-users-table
--comment: 사용자 테이블 생성 - 전화번호로 식별되는 키다리스튜디오 사원 정보

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    coin_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_coin_count_range CHECK (coin_count >= 0 AND coin_count <= 3)
);

CREATE INDEX idx_users_phone_number ON users(phone_number);

--rollback DROP TABLE users;