--liquibase formatted sql

--changeset vacation-coupon-lottery-system:05-insert-initial-system-config
--comment: 시스템 초기 설정값 삽입 - 전체 코인 수량, 사용자당 최대 코인 수, 당첨자 수 등

INSERT INTO system_config (config_key, config_value, description) VALUES
('TOTAL_COINS', '900', '전체 응모 코인 수량'),
('REMAINING_COINS', '900', '남은 응모 코인 수량'),
('MAX_COINS_PER_USER', '3', '사용자당 최대 응모 코인 수'),
('WINNERS_PER_COUPON', '3', '쿠폰당 당첨자 수');

--rollback DELETE FROM system_config WHERE config_key IN ('TOTAL_COINS', 'REMAINING_COINS', 'MAX_COINS_PER_USER', 'WINNERS_PER_COUPON');