package com.sleekydz86.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final JdbcTemplate jdbcTemplate;

    public List<String> getRecommendationsForUser(String userId) {
        try {

            String sql = """
                SELECT DISTINCT ube2.item_id
                FROM user_behavior_events ube1
                JOIN user_behavior_events ube2 ON ube1.user_id = ube2.user_id
                WHERE ube1.user_id = ? 
                AND ube2.item_id NOT IN (
                    SELECT item_id FROM user_behavior_events 
                    WHERE user_id = ? AND action_type = 'PURCHASE'
                )
                AND ube2.action_type IN ('VIEW', 'CLICK', 'LIKE')
                GROUP BY ube2.item_id
                ORDER BY COUNT(*) DESC
                LIMIT 10
                """;

            List<String> recommendations = jdbcTemplate.queryForList(sql, String.class, userId, userId);

            if (recommendations.size() < 5) {
                List<String> popularItems = getPopularItems();
                recommendations.addAll(popularItems.stream()
                        .filter(item -> !recommendations.contains(item))
                        .limit(5 - recommendations.size())
                        .collect(Collectors.toList()));
            }

            log.debug("사용자 {}에 대한 추천 아이템 {}개 생성", userId, recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("추천 생성 실패: userId={}", userId, e);
            return getPopularItems().stream().limit(5).collect(Collectors.toList());
        }
    }

    public String getUserSegment(String userId) {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_events,
                    SUM(CASE WHEN action_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchases,
                    AVG(duration) as avg_duration
                FROM user_behavior_events 
                WHERE user_id = ?
                """;

            Map<String, Object> userStats = jdbcTemplate.queryForMap(sql, userId);

            int totalEvents = ((Number) userStats.get("total_events")).intValue();
            int purchases = ((Number) userStats.get("purchases")).intValue();
            double avgDuration = userStats.get("avg_duration") != null ?
                    ((Number) userStats.get("avg_duration")).doubleValue() : 0.0;

            if (purchases >= 10) {
                return "VIP";
            } else if (purchases >= 5) {
                return "Frequent";
            } else if (totalEvents >= 50) {
                return "Active";
            } else if (avgDuration > 300) {
                return "Engaged";
            } else {
                return "New";
            }

        } catch (Exception e) {
            log.error("사용자 세그먼트 분석 실패: userId={}", userId, e);
            return "Unknown";
        }
    }

    private List<String> getPopularItems() {
        try {
            String sql = """
                SELECT item_id
                FROM item_popularity
                ORDER BY popularity_score DESC
                LIMIT 20
                """;

            return jdbcTemplate.queryForList(sql, String.class);

        } catch (Exception e) {
            log.error("인기 아이템 조회 실패", e);
            return List.of("item1", "item2", "item3", "item4", "item5");
        }
    }

    public List<String> getCategoryRecommendations(String userId, String category) {
        try {
            String sql = """
                SELECT DISTINCT ube2.item_id
                FROM user_behavior_events ube1
                JOIN user_behavior_events ube2 ON ube1.user_id = ube2.user_id
                WHERE ube1.user_id = ? 
                AND ube2.category = ?
                AND ube2.item_id NOT IN (
                    SELECT item_id FROM user_behavior_events 
                    WHERE user_id = ? AND action_type = 'PURCHASE'
                )
                AND ube2.action_type IN ('VIEW', 'CLICK', 'LIKE')
                GROUP BY ube2.item_id
                ORDER BY COUNT(*) DESC
                LIMIT 10
                """;

            return jdbcTemplate.queryForList(sql, String.class, userId, category, userId);

        } catch (Exception e) {
            log.error("카테고리별 추천 생성 실패: userId={}, category={}", userId, category, e);
            return List.of();
        }
    }

    public Map<String, Object> getUserPreferences(String userId) {
        try {
            String sql = """
                SELECT 
                    category,
                    COUNT(*) as interaction_count,
                    AVG(rating) as avg_rating,
                    SUM(CASE WHEN action_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchase_count
                FROM user_behavior_events 
                WHERE user_id = ? AND category IS NOT NULL
                GROUP BY category
                ORDER BY interaction_count DESC
                """;

            List<Map<String, Object>> preferences = jdbcTemplate.queryForList(sql, userId);

            return Map.of(
                    "preferredCategories", preferences,
                    "totalCategories", preferences.size(),
                    "mostPreferredCategory", preferences.isEmpty() ? null : preferences.get(0).get("category")
            );

        } catch (Exception e) {
            log.error("사용자 선호도 분석 실패: userId={}", userId, e);
            return Map.of();
        }
    }
}