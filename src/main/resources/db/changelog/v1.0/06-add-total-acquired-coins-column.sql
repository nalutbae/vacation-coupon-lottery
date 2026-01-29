--liquibase formatted sql

--changeset vacation-coupon-lottery-system:06-add-total-acquired-coins-column
--comment: 사용자 테이블에 누적 획득 코인 수 컬럼 추가

-- 누적 획득 코인 수 컬럼 추가
ALTER TABLE users ADD COLUMN total_acquired_coins INTEGER NOT NULL DEFAULT 0;

-- 기존 사용자들의 누적 획득 코인 수를 현재 보유 코인 수로 초기화
-- (기존 데이터의 경우 현재 보유 코인 = 누적 획득 코인으로 가정)
UPDATE users SET total_acquired_coins = coin_count WHERE coin_count > 0;

-- 컬럼에 대한 코멘트 추가
COMMENT ON COLUMN users.total_acquired_coins IS '사용자가 지금까지 획득한 총 코인 수 (최대 3개)';