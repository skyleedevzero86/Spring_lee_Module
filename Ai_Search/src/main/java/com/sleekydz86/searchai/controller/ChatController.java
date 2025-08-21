package com.sleekydz86.searchai.controller;

import com.sleekydz86.searchai.global.beans.ChatEntity;
import com.sleekydz86.searchai.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<String> streamChat(@RequestBody ChatEntity chatEntity) {
        try {
            log.info("사용자 [{}]로부터 채팅 요청 수신: {}",
                    chatEntity.getCurrentUserName(),
                    chatEntity.getMessage());

            chatService.streamChat(chatEntity);

            return ResponseEntity.ok("채팅 요청이 처리되기 시작했습니다. SSE를 통해 응답을 받으세요.");

        } catch (Exception e) {
            log.error("채팅 요청 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("채팅 요청 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat service is running");
    }
}