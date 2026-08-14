# DDL for topic microservice (plip_topic).
# Cross-service refs are logical (no FK).

CREATE TABLE topic (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_uuid   BINARY(16) NOT NULL COMMENT 'UUIDv7',
    agit_uuid    BINARY(16) NOT NULL COMMENT '아지트 서비스 논리적 참조 (no FK)',
    creator_uuid BINARY(16) NOT NULL COMMENT '유저 서비스 논리적 참조 (no FK)',
    title        VARCHAR(255) NULL,
    start_at     DATETIME(6) NULL COMMENT '진행 날짜. 기본값 오늘, 해당 비디오 날짜와 동일',
    layout       VARCHAR(50) NULL COMMENT '그룹 영상 뷰어 레이아웃',
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    deleted_at   DATETIME(6) NULL,
    UNIQUE KEY uk_topic_uuid (topic_uuid),
    KEY idx_topic_agit_deleted_start (agit_uuid, deleted_at, start_at) COMMENT 'agit 목록: agit_uuid + deleted_at IS NULL + start_at DESC'
);

CREATE TABLE topic_video (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id   BIGINT NOT NULL,
    video_uuid BINARY(16) NOT NULL COMMENT '미디어 서비스 논리적 참조 (no FK)',
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    UNIQUE KEY uk_topic_video (topic_id, video_uuid),
    CONSTRAINT fk_topic_video_topic FOREIGN KEY (topic_id) REFERENCES topic (id)
);
