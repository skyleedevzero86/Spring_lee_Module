package com.sleekydz86.kmsage.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageSenderFactory {

    @Autowired
    private List<MessageSender> messageSenders;

    public MessageSender getMessageSender(String messageType) {
        return messageSenders.stream()
                .filter(sender -> sender.supports(messageType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 메시지 타입: " + messageType));
    }
}