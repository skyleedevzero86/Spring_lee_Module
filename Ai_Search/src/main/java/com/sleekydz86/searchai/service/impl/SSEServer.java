package com.sleekydz86.searchai.service.impl;

import com.sleekydz86.searchai.global.enums.SSEMsgType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Slf4j
public class SSEServer {

    private static final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public static void addClient(String userId, SseEmitter sseEmitter) {
        sseEmitterMap.put(userId, sseEmitter);
        log.info("사용자 [{}]의 SSE 연결이 생성되었습니다.", userId);
    }

    public static void sendMsg(String userId, String message, SSEMsgType msgType) {
        SseEmitter sseEmitter = sseEmitterMap.get(userId);
        if (sseEmitter != null) {
            try {
                SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
                        .data(message)
                        .name(msgType.name().toLowerCase());

                sseEmitter.send(eventBuilder);
                log.debug("사용자 [{}]에게 메시지 전송 완료: [{}] {}", userId, msgType, message);
            } catch (Exception e) {
                log.error("사용자 [{}]에게 메시지 전송 실패: {}", userId, e.getMessage());
                removeClient(userId);
            }
        } else {
            log.warn("사용자 [{}]의 SSE 연결을 찾을 수 없습니다.", userId);
        }
    }

    public static void close(String userId) {
        SseEmitter sseEmitter = sseEmitterMap.get(userId);
        if (sseEmitter != null) {
            try {
                sseEmitter.complete();
                log.info("사용자 [{}]의 SSE 연결이 정상 종료되었습니다.", userId);
            } catch (Exception e) {
                log.error("사용자 [{}]의 SSE 연결 종료 중 오류 발생: {}", userId, e.getMessage());
            } finally {
                removeClient(userId);
            }
        }
    }

    private static void removeClient(String userId) {
        sseEmitterMap.remove(userId);
        log.info("사용자 [{}]의 SSE 연결이 제거되었습니다.", userId);
    }

    public static void closeAll() {
        sseEmitterMap.forEach((userId, sseEmitter) -> {
            try {
                sseEmitter.complete();
            } catch (Exception e) {
                log.error("사용자 [{}]의 SSE 연결 강제 종료 중 오류 발생: {}", userId, e.getMessage());
            }
        });
        sseEmitterMap.clear();
        log.info("모든 SSE 연결이 종료되었습니다.");
    }

    public static int getActiveConnectionCount() {
        return sseEmitterMap.size();
    }
}