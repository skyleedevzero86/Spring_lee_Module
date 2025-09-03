package com.sleekydz86.global.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeDashboardHandler implements WebSocketHandler {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket 연결 생성: {}", session.getId());

        sendDashboardData(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getPayload();
            log.debug("WebSocket 메시지 수신: {}", payload);

            if ("getStats".equals(payload)) {
                sendDashboardData(session);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 전송 오류: {}", session.getId(), exception);
        sessions.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session.getId());
        log.info("WebSocket 연결 종료: {}, 상태: {}", session.getId(), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void sendDashboardData(WebSocketSession session) {
        try {
            Map<String, Object> dashboardData = getRealtimeDashboardData();
            String jsonData = objectMapper.writeValueAsString(dashboardData);

            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonData));
            }
        } catch (IOException e) {
            log.error("대시보드 데이터 전송 실패: {}", session.getId(), e);
        }
    }

    private Map<String, Object> getRealtimeDashboardData() {
        try {
            int activeUsers = getActiveUsers();
            int eventsPerSecond = getEventsPerSecond();
            List<Map<String, Object>> topCategories = getTopCategories();
            List<Map<String, Object>> recentEvents = getRecentEvents();
            List<Map<String, Object>> topItems = getTopItems();

            return Map.of(
                    "timestamp", System.currentTimeMillis(),
                    "activeUsers", activeUsers,
                    "eventsPerSecond", eventsPerSecond,
                    "topCategories", topCategories,
                    "recentEvents", recentEvents,
                    "topItems", topItems,
                    "totalEvents", getTotalEvents(),
                    "totalUsers", getTotalUsers()
            );

        } catch (Exception e) {
            log.error("실시간 대시보드 데이터 조회 실패", e);
            return Map.of("error", "데이터 조회 실패");
        }
    }

    private int getActiveUsers() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT user_id) FROM user_behavior_events WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)",
                    Integer.class
            );
        } catch (Exception e) {
            log.error("활성 사용자 수 조회 실패", e);
            return 0;
        }
    }

    private int getEventsPerSecond() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_behavior_events WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)",
                    Integer.class
            );
        } catch (Exception e) {
            log.error("초당 이벤트 수 조회 실패", e);
            return 0;
        }
    }

    private List<Map<String, Object>> getTopCategories() {
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
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("인기 카테고리 조회 실패", e);
            return List.of();
        }
    }

    private List<Map<String, Object>> getRecentEvents() {
        try {
            String sql = """
                SELECT user_id, item_id, action_type, category, timestamp
                FROM user_behavior_events 
                ORDER BY timestamp DESC 
                LIMIT 10
                """;
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("최근 이벤트 조회 실패", e);
            return List.of();
        }
    }

    private List<Map<String, Object>> getTopItems() {
        try {
            String sql = """
                SELECT item_id, category, popularity_score
                FROM item_popularity
                ORDER BY popularity_score DESC
                LIMIT 10
                """;
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("인기 아이템 조회 실패", e);
            return List.of();
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

    private int getTotalUsers() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT user_id) FROM user_behavior_events", Integer.class);
        } catch (Exception e) {
            log.error("총 사용자 수 조회 실패", e);
            return 0;
        }
    }

    public void startRealtimeUpdates() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!sessions.isEmpty()) {
                Map<String, Object> data = getRealtimeDashboardData();
                broadcastToAllSessions(data);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void broadcastToAllSessions(Map<String, Object> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            TextMessage message = new TextMessage(jsonData);

            sessions.values().removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                        return false;
                    }
                } catch (IOException e) {
                    log.error("브로드캐스트 전송 실패: {}", session.getId(), e);
                }
                return true; // 연결이 끊어진 세션 제거
            });
        } catch (Exception e) {
            log.error("브로드캐스트 데이터 직렬화 실패", e);
        }
    }

    public void stopRealtimeUpdates() {
        scheduler.shutdown();
    }
}