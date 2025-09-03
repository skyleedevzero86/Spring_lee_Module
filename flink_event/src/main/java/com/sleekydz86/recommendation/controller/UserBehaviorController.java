package com.sleekydz86.recommendation.controller;

import com.sleekydz86.recommendation.model.UserBehaviorEvent;
import com.sleekydz86.recommendation.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/behavior")
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorService behaviorService;

    @PostMapping("/track")
    public ResponseEntity<String> trackUserBehavior(@RequestBody UserBehaviorEvent event) {
        try {
            event.setTimestamp(LocalDateTime.now());
            event.setSessionId(UUID.randomUUID().toString());

            behaviorService.sendBehaviorEvent(event);

            log.info("사용자 행동 추적: userId={}, action={}, itemId={}",
                    event.getUserId(), event.getActionType(), event.getItemId());

            return ResponseEntity.ok("이벤트가 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            log.error("이벤트 전송 실패", e);
            return ResponseEntity.internalServerError()
                    .body("이벤트 전송에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<?> getRecommendations(@PathVariable String userId) {
        return ResponseEntity.ok(behaviorService.getRecommendations(userId));
    }
}