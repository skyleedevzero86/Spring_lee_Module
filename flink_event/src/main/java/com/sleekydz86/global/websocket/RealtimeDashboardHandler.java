package com.sleekydz86.global.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
    private ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;

    @PostConstruct
    public void init() {
        scheduler = Executors.newScheduledThreadPool(2);
        log.info("RealtimeDashboardHandler 초기화 완료");
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("RealtimeDashboardHandler 종료");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket 연결됨: {}", session.getId());
        sendDashboardStats(session);

        if (!isRunning) {
            startRealtimeUpdates();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            log.debug("메시지 수신: {}", payload);

            if ("getStats".equals(payload)) {
                sendDashboardStats(session);
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

        if (sessions.isEmpty() && isRunning) {
            stopRealtimeUpdates();
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void startRealtimeUpdates() {
        if (isRunning) return;

        isRunning = true;
        log.info("실시간 업데이트 시작");
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!sessions.isEmpty()) {
                    broadcastDashboardStats();
                }
            } catch (Exception e) {
                log.error("실시간 업데이트 오류", e);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private void stopRealtimeUpdates() {
        if (!isRunning) return;

        isRunning = false;
        log.info("실시간 업데이트 중지");
    }

    private void broadcastDashboardStats() {
        if (sessions.isEmpty()) return;

        try {
            Map<String, Object> stats = getDashboardStats();
            String jsonStats = objectMapper.writeValueAsString(stats);

            sessions.entrySet().removeIf(entry -> {
                WebSocketSession session = entry.getValue();
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonStats));
                        return false;
                    } else {
                        return true;
                    }
                } catch (IOException e) {
                    log.error("메시지 전송 실패: {}", session.getId(), e);
                    return true;
                }
            });

        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패", e);
        }
    }

    private void sendDashboardStats(WebSocketSession session) {
        try {
            Map<String, Object> stats = getDashboardStats();
            String jsonStats = objectMapper.writeValueAsString(stats);
            session.sendMessage(new TextMessage(jsonStats));
        } catch (Exception e) {
            log.error("대시보드 통계 전송 실패", e);
        }
    }

    private Map<String, Object> getDashboardStats() {
        try {
            int activeUsers = getActiveUsers();
            int eventsPerSecond = getEventsPerSecond();
            Map<String, Integer> topCategories = getTopCategories();
            List<Map<String, Object>> recentEvents = getRecentEvents();

            return Map.of(
                    "activeUsers", activeUsers,
                    "eventsPerSecond", eventsPerSecond,
                    "topCategories", topCategories,
                    "recentEvents", recentEvents,
                    "timestamp", System.currentTimeMillis()
            );
        } catch (Exception e) {
            log.error("대시보드 통계 조회 중 오류", e);
            return Map.of("error", "데이터 조회 실패");
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
}