--liquibase formatted sql

--changeset vacation-coupon-lottery-system:03-create-system-config-table
--comment: 시스템 설정 테이블 생성 - 전체 코인 수량, 당첨자 수 등 시스템 설정값 관리

CREATE TABLE system_config (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE system_config;