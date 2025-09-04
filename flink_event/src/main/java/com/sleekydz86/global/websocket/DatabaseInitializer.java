package com.sleekydz86.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("데이터베이스 초기화 시작");
            initializeTables();
            insertSampleData();
            log.info("데이터베이스 초기화 완료");
        } catch (Exception e) {
            log.error("데이터베이스 초기화 중 오류 발생", e);
        }
    }

    private void initializeTables() {
        try {
            String createUserBehaviorTable = """
                CREATE TABLE IF NOT EXISTS user_behavior_events (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id VARCHAR(255) NOT NULL,
                    item_id VARCHAR(255) NOT NULL,
                    action_type VARCHAR(50) NOT NULL,
                    category VARCHAR(100),
                    rating DECIMAL(3,2),
                    duration BIGINT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    session_id VARCHAR(255),
                    device_type VARCHAR(50),
                    location VARCHAR(100),
                    referrer VARCHAR(500),
                    price DECIMAL(10,2),
                    platform VARCHAR(50),
                    INDEX idx_user_id (user_id),
                    INDEX idx_item_id (item_id),
                    INDEX idx_timestamp (timestamp),
                    INDEX idx_action_type (action_type),
                    INDEX idx_category (category)
                )
                """;

            jdbcTemplate.execute(createUserBehaviorTable);
            log.info("user_behavior_events 테이블 생성 완료");
            String createItemPopularityTable = """
                CREATE TABLE IF NOT EXISTS item_popularity (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    item_id VARCHAR(255) NOT NULL UNIQUE,
                    category VARCHAR(100),
                    popularity_score DECIMAL(10,2) DEFAULT 0.0,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_popularity_score (popularity_score),
                    INDEX idx_category (category)
                )
                """;

            jdbcTemplate.execute(createItemPopularityTable);
            log.info("item_popularity 테이블 생성 완료");
            String createUserBehaviorStatsTable = """
                CREATE TABLE IF NOT EXISTS user_behavior_stats (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id VARCHAR(255) NOT NULL,
                    action_type VARCHAR(50) NOT NULL,
                    count BIGINT DEFAULT 0,
                    window_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_user_action (user_id, action_type, window_time),
                    INDEX idx_user_id (user_id),
                    INDEX idx_action_type (action_type),
                    INDEX idx_window_time (window_time)
                )
                """;

            jdbcTemplate.execute(createUserBehaviorStatsTable);
            log.info("user_behavior_stats 테이블 생성 완료");

        } catch (Exception e) {
            log.error("테이블 생성 중 오류 발생", e);
        }
    }

    private void insertSampleData() {
        try {
            String countSql = "SELECT COUNT(*) FROM user_behavior_events";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);

            if (count == null || count == 0) {
                log.info("샘플 데이터를 삽입합니다.");
                String[] sampleEvents = {
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user1', 'item1', 'VIEW', 'Electronics', 4.5, 120, 'session1', 'mobile', 'Seoul', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user1', 'item2', 'CLICK', 'Electronics', 4.2, 85, 'session1', 'mobile', 'Seoul', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user1', 'item3', 'PURCHASE', 'Electronics', 4.8, 200, 'session1', 'mobile', 'Seoul', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user2', 'item1', 'VIEW', 'Electronics', 4.3, 95, 'session2', 'desktop', 'Busan', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user2', 'item4', 'CLICK', 'Books', 4.0, 110, 'session2', 'desktop', 'Busan', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user3', 'item5', 'VIEW', 'Clothing', 3.8, 75, 'session3', 'tablet', 'Incheon', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user3', 'item6', 'LIKE', 'Clothing', 4.1, 60, 'session3', 'tablet', 'Incheon', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user4', 'item2', 'VIEW', 'Electronics', 4.6, 140, 'session4', 'mobile', 'Daegu', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user4', 'item7', 'PURCHASE', 'Books', 4.4, 180, 'session4', 'mobile', 'Daegu', 'web')",
                        "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, rating, duration, session_id, device_type, location, platform) VALUES ('user5', 'item8', 'VIEW', 'Sports', 3.9, 100, 'session5', 'desktop', 'Gwangju', 'web')"
                };

                for (String sql : sampleEvents) {
                    jdbcTemplate.execute(sql);
                }
                String[] samplePopularity = {
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item1', 'Electronics', 15.5) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)",
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item2', 'Electronics', 12.3) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)",
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item3', 'Electronics', 18.7) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)",
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item4', 'Books', 8.9) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)",
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item5', 'Clothing', 6.2) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)",
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item6', 'Clothing', 7.8) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)",
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item7', 'Books', 11.4) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)",
                        "INSERT INTO item_popularity (item_id, category, popularity_score) VALUES ('item8', 'Sports', 5.6) ON DUPLICATE KEY UPDATE popularity_score = VALUES(popularity_score)"
                };

                for (String sql : samplePopularity) {
                    jdbcTemplate.execute(sql);
                }

                log.info("샘플 데이터 삽입 완료");
            } else {
                log.info("이미 데이터가 존재합니다. 샘플 데이터 삽입을 건너뜁니다.");
            }

        } catch (Exception e) {
            log.error("샘플 데이터 삽입 중 오류 발생", e);
        }
    }
}