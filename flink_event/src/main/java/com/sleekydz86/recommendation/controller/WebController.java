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
        model.addAttribute("totalUsers", getTotalUsers());
        model.addAttribute("totalEvents", getTotalEvents());
        model.addAttribute("totalItems", getTotalItems());
        model.addAttribute("todayEvents", getTodayEvents());

        return "dashboard/index";
    }

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "") String category,
                           @RequestParam(defaultValue = "1") int page,
                           Model model) {

        List<Map<String, Object>> products = getProducts(category, page);
        List<String> categories = getCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentPage", page);

        return "products/list";
    }

    @GetMapping("/products/{itemId}")
    public String productDetail(@PathVariable String itemId, Model model) {
        Map<String, Object> product = getProductDetail(itemId);
        List<Map<String, Object>> relatedProducts = getRelatedProducts(itemId);

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);

        return "products/detail";
    }

    @GetMapping("/recommendations/{userId}")
    public String userRecommendations(@PathVariable String userId, Model model) {
        List<String> recommendations = recommendationService.getRecommendationsForUser(userId);
        String userSegment = recommendationService.getUserSegment(userId);
        Map<String, Object> userStats = getUserStats(userId);

        model.addAttribute("userId", userId);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("userSegment", userSegment);
        model.addAttribute("userStats", userStats);

        return "recommendations/user";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("realtimeStats", getRealtimeStats());
        model.addAttribute("topItems", getTopItems());
        model.addAttribute("topCategories", getTopCategories());

        return "admin/dashboard";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        return "analytics/index";
    }

    @ResponseBody
    @GetMapping("/api/dashboard/stats")
    public Map<String, Object> getDashboardStats() {
        return Map.of(
                "activeUsers", getActiveUsers(),
                "eventsPerSecond", getEventsPerSecond(),
                "topCategories", getTopCategories(),
                "recentEvents", getRecentEvents()
        );
    }

    @ResponseBody
    @GetMapping("/api/analytics/hourly")
    public List<Map<String, Object>> getHourlyAnalytics() {
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
    }

    @ResponseBody
    @GetMapping("/api/analytics/categories")
    public List<Map<String, Object>> getCategoryAnalytics() {
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
    }

    private int getTotalUsers() {
        return jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT user_id) FROM user_behavior_events", Integer.class);
    }

    private long getTotalEvents() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_behavior_events", Long.class);
    }

    private int getTotalItems() {
        return jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT item_id) FROM user_behavior_events", Integer.class);
    }

    private int getTodayEvents() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_behavior_events WHERE DATE(timestamp) = CURDATE()",
                Integer.class
        );
    }

    private List<Map<String, Object>> getProducts(String category, int page) {
        String sql = """
            SELECT DISTINCT 
                ube.item_id,
                ube.category,
                ip.popularity_score,
                COUNT(*) as view_count,
                AVG(ube.rating) as avg_rating
            FROM user_behavior_events ube
            LEFT JOIN item_popularity ip ON ube.item_id = ip.item_id
            WHERE (? = '' OR ube.category = ?)
            GROUP BY ube.item_id, ube.category, ip.popularity_score
            ORDER BY ip.popularity_score DESC, view_count DESC
            LIMIT 20 OFFSET ?
            """;

        return jdbcTemplate.queryForList(sql, category, category, (page - 1) * 20);
    }

    private List<String> getCategories() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT category FROM user_behavior_events WHERE category IS NOT NULL",
                String.class
        );
    }

    private Map<String, Object> getProductDetail(String itemId) {
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
    }

    private List<Map<String, Object>> getRelatedProducts(String itemId) {
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
    }

    private Map<String, Object> getUserStats(String userId) {
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
    }

    private Map<String, Object> getRealtimeStats() {
        return Map.of(
                "activeUsers", getActiveUsers(),
                "eventsPerSecond", getEventsPerSecond(),
                "totalEvents", getTotalEvents()
        );
    }

    private List<Map<String, Object>> getTopItems() {
        String sql = """
            SELECT item_id, category, popularity_score
            FROM item_popularity
            ORDER BY popularity_score DESC
            LIMIT 10
            """;

        return jdbcTemplate.queryForList(sql);
    }

    private Map<String, Integer> getTopCategories() {
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
                        row -> ((Number) row.get("count")).intValue()
                ));
    }

    private int getActiveUsers() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT user_id) FROM user_behavior_events WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)",
                Integer.class
        );
    }

    private int getEventsPerSecond() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_behavior_events WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)",
                Integer.class
        );
    }

    private List<Map<String, Object>> getRecentEvents() {
        String sql = """
            SELECT user_id, item_id, action_type, category, timestamp
            FROM user_behavior_events 
            ORDER BY timestamp DESC 
            LIMIT 20
            """;

        return jdbcTemplate.queryForList(sql);
    }
}