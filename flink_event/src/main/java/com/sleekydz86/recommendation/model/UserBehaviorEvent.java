package com.sleekydz86.recommendation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorEvent {
    private String userId;
    private String itemId;
    private String actionType;
    private String category;
    private Double rating;
    private Long duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String sessionId;
    private String deviceType;
    private String location;

    private String referrer;
    private Double price;
    private String platform;
}