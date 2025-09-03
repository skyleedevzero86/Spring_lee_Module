package com.sleekydz86.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.recommendation.model.UserBehaviorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RecommendationService recommendationService;

    private static final String BEHAVIOR_TOPIC = "user-behavior-events";

    public void sendBehaviorEvent(UserBehaviorEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);

            CompletableFuture.runAsync(() -> {
                kafkaTemplate.send(BEHAVIOR_TOPIC, event.getUserId(), jsonEvent)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Kafka 전송 실패: {}", ex.getMessage());
                            } else {
                                log.debug("Kafka 전송 성공: partition={}, offset={}",
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            }
                        });
            });

        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패", e);
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }

    public List<String> getRecommendations(String userId) {
        return recommendationService.getRecommendationsForUser(userId);
    }
}