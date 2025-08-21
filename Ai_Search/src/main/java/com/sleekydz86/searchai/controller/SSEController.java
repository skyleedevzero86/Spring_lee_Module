package com.sleekydz86.searchai.controller;

import com.sleekydz86.searchai.service.impl.SSEServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@Slf4j
public class SSEController {

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String userId) {
        log.info("사용자 [{}]가 SSE 연결을 요청했습니다.", userId);

        SseEmitter sseEmitter = new SseEmitter(30 * 60 * 1000L);

        sseEmitter.onCompletion(() -> {
            log.info("사용자 [{}]의 SSE 연결이 완료되었습니다.", userId);
            SSEServer.close(userId);
        });

        sseEmitter.onTimeout(() -> {
            log.warn("사용자 [{}]의 SSE 연결이 타임아웃되었습니다.", userId);
            SSEServer.close(userId);
        });

        sseEmitter.onError(throwable -> {
            log.error("사용자 [{}]의 SSE 연결에서 오류 발생: {}", userId, throwable.getMessage());
            SSEServer.close(userId);
        });

        SSEServer.addClient(userId, sseEmitter);

        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established"));
        } catch (Exception e) {
            log.error("사용자 [{}]에게 연결 확인 메시지 전송 실패: {}", userId, e.getMessage());
        }

        return sseEmitter;
    }

    @GetMapping("/status")
    public int getConnectionStatus() {
        int activeConnections = SSEServer.getActiveConnectionCount();
        log.info("현재 활성 SSE 연결 수: {}", activeConnections);
        return activeConnections;
    }
}