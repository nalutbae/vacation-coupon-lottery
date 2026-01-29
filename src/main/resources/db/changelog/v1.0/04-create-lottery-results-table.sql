--liquibase formatted sql

--changeset vacation-coupon-lottery-system:04-create-lottery-results-table
--comment: 추첨 결과 테이블 생성 - 당첨자 정보 및 추첨 결과 저장

CREATE TABLE lottery_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    coupon_type VARCHAR(20) NOT NULL,
    rank INTEGER NOT NULL,
    lottery_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lottery_results_entry_id FOREIGN KEY (entry_id) REFERENCES vacation_coupon_entries(id) ON DELETE CASCADE,
    CONSTRAINT chk_lottery_results_coupon_type CHECK (coupon_type IN ('ONE_DAY', 'THREE_DAY')),
    CONSTRAINT chk_lottery_results_rank CHECK (rank >= 1 AND rank <= 3)
);

CREATE INDEX idx_lottery_results_entry_id ON lottery_results(entry_id);
CREATE INDEX idx_lottery_results_coupon_type ON lottery_results(coupon_type);
CREATE INDEX idx_lottery_results_rank ON lottery_results(rank);

--rollback DROP TABLE lottery_results;