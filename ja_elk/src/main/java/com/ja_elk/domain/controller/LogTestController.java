package com.ja_elk.domain.controller;

import com.ja_elk.domain.model.User;
import com.ja_elk.domain.service.UserService;
import com.ja_elk.domain.service.FluentdLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LogTestController {
    private static final Logger logger = LoggerFactory.getLogger(LogTestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FluentdLoggingService fluentdLoggingService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("테스트 API 호출됨");
        logger.debug("디버그 메시지: 테스트 요청 처리 중");
        logger.warn("경고 메시지: 이것은 테스트용 경고입니다");

        // Fluentd로도 로그 전송
        fluentdLoggingService.logInfo("테스트 API 호출됨", this.getClass().getName());
        fluentdLoggingService.logWarn("경고 메시지: 이것은 테스트용 경고입니다", this.getClass().getName());

        return ResponseEntity.ok("테스트 성공!");
    }

    @PostMapping("/generate-logs/{count}")
    public ResponseEntity<Map<String, Object>> generateLogs(@PathVariable int count) {
        logger.info("로그 생성 요청: {}개", count);
        fluentdLoggingService.logInfo("로그 생성 요청: " + count + "개", this.getClass().getName());

        try {
            List<User> users = userService.generateDummyUsers(count);

            Map<String, Object> response = Map.of(
                    "message", "로그 생성 완료",
                    "userCount", users.size(),
                    "timestamp", System.currentTimeMillis()
            );

            logger.info("로그 생성 작업 완료: {}개 사용자 처리됨", users.size());
            fluentdLoggingService.logInfo("로그 생성 작업 완료: " + users.size() + "개 사용자 처리됨", this.getClass().getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("로그 생성 중 오류 발생", e);
            fluentdLoggingService.logError("로그 생성 중 오류 발생", this.getClass().getName(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "로그 생성 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        logger.info("사용자 조회 API 호출: ID = {}", id);
        fluentdLoggingService.logInfo("사용자 조회 API 호출: ID = " + id, this.getClass().getName());

        try {
            User user = userService.findById(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                logger.warn("사용자를 찾을 수 없음: ID = {}", id);
                fluentdLoggingService.logWarn("사용자를 찾을 수 없음: ID = " + id, this.getClass().getName());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("사용자 조회 중 오류 발생: ID = {}", id, e);
            fluentdLoggingService.logError("사용자 조회 중 오류 발생: ID = " + id, this.getClass().getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/stress-test")
    public ResponseEntity<String> stressTest() {
        logger.info("스트레스 테스트 시작");
        fluentdLoggingService.logInfo("스트레스 테스트 시작", this.getClass().getName());

        for (int i = 0; i < 100; i++) {
            logger.info("스트레스 테스트 로그 {}/100", i + 1);
            logger.debug("상세 디버그 정보: 반복 {} 처리 중", i);

            if (i % 10 == 0) {
                logger.warn("10번째마다 경고: 진행률 {}%", i);
                fluentdLoggingService.logWarn("10번째마다 경고: 진행률 " + i + "%", this.getClass().getName());
            }

            if (i % 50 == 0) {
                logger.error("50번째마다 에러 시뮬레이션: 반복 {}", i);
                fluentdLoggingService.logError("50번째마다 에러 시뮬레이션: 반복 " + i, this.getClass().getName(), null);
            }
        }

        logger.info("스트레스 테스트 완료");
        fluentdLoggingService.logInfo("스트레스 테스트 완료", this.getClass().getName());
        return ResponseEntity.ok("스트레스 테스트 완료!");
    }
}