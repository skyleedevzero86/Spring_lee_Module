package com.sleekydz86.recommendation.controller;

import com.sleekydz86.recommendation.service.RecommendationService;
import com.sleekydz86.recommendation.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    private final RecommendationService recommendationService;
    private final UserBehaviorService behaviorService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("totalUsers", getTotalUsers());
            model.addAttribute("totalEvents", getTotalEvents());
            model.addAttribute("totalItems", getTotalItems());
            model.addAttribute("todayEvents", getTodayEvents());
        } catch (Exception e) {
            log.error("대시보드 데이터 로드 실패", e);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalEvents", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("todayEvents", 0);
        }

        return "dashboard/index";
    }

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        try {
            List<Map<String, Object>> products = getProducts(category, page);
            List<String> categories = getCategories();

            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("currentPage", page);
        } catch (Exception e) {
            log.error("상품 목록 로드 실패", e);
            model.addAttribute("products", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("selectedCategory", category);
            model.addAttribute("currentPage", page);
        }

        return "products/list";
    }

    @GetMapping("/products/{itemId}")
    public String productDetail(@PathVariable String itemId, Model model) {
        try {
            Map<String, Object> product = getProductDetail(itemId);
            List<Map<String, Object>> relatedProducts = getRelatedProducts(itemId);

            model.addAttribute("product", product);
            model.addAttribute("relatedProducts", relatedProducts);
        } catch (Exception e) {
            log.error("상품 상세 로드 실패: itemId={}", itemId, e);
            model.addAttribute("product", Map.of());
            model.addAttribute("relatedProducts", List.of());
        }

        return "products/detail";
    }

    @GetMapping("/recommendations/{userId}")
    public String userRecommendations(@PathVariable String userId, Model model) {
        try {
            List<String> recommendations = recommendationService.getRecommendationsForUser(userId);
            String userSegment = recommendationService.getUserSegment(userId);
            Map<String, Object> userStats = getUserStats(userId);

            model.addAttribute("userId", userId);
            model.addAttribute("recommendations", recommendations);
            model.addAttribute("userSegment", userSegment);
            model.addAttribute("userStats", userStats);
        } catch (Exception e) {
            log.error("사용자 추천 로드 실패: userId={}", userId, e);
            model.addAttribute("userId", userId);
            model.addAttribute("recommendations", List.of());
            model.addAttribute("userSegment", "Unknown");
            model.addAttribute("userStats", Map.of());
        }

        return "recommendations/user";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        try {
            model.addAttribute("realtimeStats", getRealtimeStats());
            model.addAttribute("topItems", getTopItems());
            model.addAttribute("topCategories", getTopCategories());
        } catch (Exception e) {
            log.error("관리자 대시보드 로드 실패", e);
            model.addAttribute("realtimeStats", Map.of());
            model.addAttribute("topItems", List.of());
            model.addAttribute("topCategories", Map.of());
        }

        return "admin/dashboard";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        return "analytics/index";
    }

    @ResponseBody
    @GetMapping("/api/dashboard/stats")
    public Map<String, Object> getDashboardStats() {
        try {
            return Map.of(
                    "activeUsers", getActiveUsers(),
                    "eventsPerSecond", getEventsPerSecond(),
                    "topCategories", getTopCategories(),
                    "recentEvents", getRecentEvents());
        } catch (Exception e) {
            log.error("대시보드 API 오류", e);
            return Map.of("error", "데이터 조회 실패");
        }
    }

    @ResponseBody
    @GetMapping("/api/analytics/hourly")
    public List<Map<String, Object>> getHourlyAnalytics() {
        try {
            String sql = """
                    SELECT
                        DATE_FORMAT(timestamp, '%H:00') as hour,
                        COUNT(*) as events,
                        COUNT(DISTINCT user_id) as unique_users,
                        SUM(CASE WHEN action_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchases
                    FROM user_behavior_events
                    WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
                    GROUP BY DATE_FORMAT(timestamp, '%H:00')
                    ORDER BY hour
                    """;

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("시간별 분석 데이터 조회 실패", e);
            return List.of();
        }
    }

    @ResponseBody
    @GetMapping("/api/analytics/categories")
    public List<Map<String, Object>> getCategoryAnalytics() {
        try {
            String sql = """
                    SELECT
                        category,
                        COUNT(*) as total_events,
                        COUNT(DISTINCT user_id) as unique_users,
                        AVG(duration) as avg_duration,
                        SUM(CASE WHEN action_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchases
                    FROM user_behavior_events
                    WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                    AND category IS NOT NULL
                    GROUP BY category
                    ORDER BY total_events DESC
                    LIMIT 10
                    """;

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("카테고리 분석 데이터 조회 실패", e);
            return List.of();
        }
    }

    private int getTotalUsers() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT user_id) FROM user_behavior_events",
                    Integer.class);
        } catch (Exception e) {
            log.error("총 사용자 수 조회 실패", e);
            return 0;
        }
    }

    private long getTotalEvents() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_behavior_events", Long.class);
        } catch (Exception e) {
            log.error("총 이벤트 수 조회 실패", e);
            return 0L;
        }
    }

    private int getTotalItems() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT item_id) FROM user_behavior_events",
                    Integer.class);
        } catch (Exception e) {
            log.error("총 상품 수 조회 실패", e);
            return 0;
        }
    }

    private int getTodayEvents() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_behavior_events WHERE DATE(timestamp) = CURDATE()",
                    Integer.class);
        } catch (Exception e) {
            log.error("오늘 이벤트 수 조회 실패", e);
            return 0;
        }
    }

    private List<Map<String, Object>> getProducts(String category, int page) {
        try {
            String sql = """
                    SELECT
                        ube.item_id,
                        ube.category,
                        COALESCE(ip.popularity_score, 0) AS popularity_score,
                        COUNT(*) AS view_count,
                        AVG(ube.rating) AS avg_rating
                    FROM user_behavior_events ube
                    LEFT JOIN item_popularity ip ON ube.item_id = ip.item_id
                    WHERE ube.category = IF(? = '', ube.category, ?)
                    GROUP BY ube.item_id, ube.category, ip.popularity_score
                    ORDER BY popularity_score DESC, view_count DESC;
                """;

            return jdbcTemplate.queryForList(sql, category, category, (page - 1) * 20);
        } catch (Exception e) {
            log.error("상품 목록 조회 실패", e);
            return List.of();
        }
    }

    private List<String> getCategories() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT DISTINCT category FROM user_behavior_events WHERE category IS NOT NULL",
                    String.class);
        } catch (Exception e) {
            log.error("카테고리 목록 조회 실패", e);
            return List.of();
        }
    }

    private Map<String, Object> getProductDetail(String itemId) {
        try {
            String sql = """
                    SELECT
                        item_id,
                        category,
                        COUNT(*) as total_views,
                        AVG(rating) as avg_rating,
                        SUM(CASE WHEN action_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchase_count
                    FROM user_behavior_events
                    WHERE item_id = ?
                    GROUP BY item_id, category
                    """;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, itemId);
            return results.isEmpty() ? Map.of() : results.get(0);
        } catch (Exception e) {
            log.error("상품 상세 조회 실패: itemId={}", itemId, e);
            return Map.of();
        }
    }

    private List<Map<String, Object>> getRelatedProducts(String itemId) {
        try {
            String sql = """
                    SELECT DISTINCT ube2.item_id, ube2.category, COUNT(*) as relevance_score
                    FROM user_behavior_events ube1
                    JOIN user_behavior_events ube2 ON ube1.user_id = ube2.user_id
                    WHERE ube1.item_id = ? AND ube2.item_id != ?
                    GROUP BY ube2.item_id, ube2.category
                    ORDER BY relevance_score DESC
                    LIMIT 6
                    """;

            return jdbcTemplate.queryForList(sql, itemId, itemId);
        } catch (Exception e) {
            log.error("관련 상품 조회 실패: itemId={}", itemId, e);
            return List.of();
        }
    }

    private Map<String, Object> getUserStats(String userId) {
        try {
            String sql = """
                    SELECT
                        COUNT(*) as total_events,
                        COUNT(DISTINCT item_id) as unique_items,
                        SUM(CASE WHEN action_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchases,
                        AVG(duration) as avg_duration
                    FROM user_behavior_events
                    WHERE user_id = ?
                    """;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);
            return results.isEmpty() ? Map.of() : results.get(0);
        } catch (Exception e) {
            log.error("사용자 통계 조회 실패: userId={}", userId, e);
            return Map.of();
        }
    }

    private Map<String, Object> getRealtimeStats() {
        try {
            return Map.of(
                    "activeUsers", getActiveUsers(),
                    "eventsPerSecond", getEventsPerSecond(),
                    "totalEvents", getTotalEvents());
        } catch (Exception e) {
            log.error("실시간 통계 조회 실패", e);
            return Map.of();
        }
    }

    private List<Map<String, Object>> getTopItems() {
        try {
            String sql = """
                    SELECT item_id, category, COALESCE(popularity_score, 0) as popularity_score
                    FROM item_popularity
                    ORDER BY popularity_score DESC
                    LIMIT 10
                    """;

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("인기 상품 조회 실패", e);
            return List.of();
        }
    }

    private Map<String, Integer> getTopCategories() {
        try {
            String sql = """
                    SELECT category, COUNT(*) as count
                    FROM user_behavior_events
                    WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
                    AND category IS NOT NULL
                    GROUP BY category
                    ORDER BY count DESC
                    LIMIT 5
                    """;

            return jdbcTemplate.queryForList(sql).stream()
                    .collect(java.util.stream.Collectors.toMap(
                            row -> (String) row.get("category"),
                            row -> ((Number) row.get("count")).intValue()));
        } catch (Exception e) {
            log.error("인기 카테고리 조회 실패", e);
            return Map.of();
        }
    }

    private int getActiveUsers() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT user_id) FROM user_behavior_events WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)",
                    Integer.class);
        } catch (Exception e) {
            log.error("활성 사용자 수 조회 실패", e);
            return 0;
        }
    }

    private int getEventsPerSecond() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_behavior_events WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)",
                    Integer.class);
        } catch (Exception e) {
            log.error("분당 이벤트 수 조회 실패", e);
            return 0;
        }
    }

    private List<Map<String, Object>> getRecentEvents() {
        try {
            String sql = """
                    SELECT user_id, item_id, action_type, category, timestamp
                    FROM user_behavior_events
                    ORDER BY timestamp DESC
                    LIMIT 20
                    """;

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("최근 이벤트 조회 실패", e);
            return List.of();
        }
    }

    @GetMapping("/flink-ui")
    public String flinkUi() {
        return "redirect:http://localhost:8080";
    }

    @ResponseBody
    @GetMapping("/api/flink/status")
    public Map<String, Object> getFlinkStatus() {
        try {
            return Map.of(
                    "status", "running",
                    "webui", "http://localhost:8080",
                    "message", "Flink Web UI는 별도로 실행해야 합니다"
            );
        } catch (Exception e) {
            log.error("Flink 상태 확인 실패", e);
            return Map.of(
                    "status", "error",
                    "message", "Flink 연결 실패: " + e.getMessage()
            );
        }
    }
}