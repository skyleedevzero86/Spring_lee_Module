package com.sleekydz86.kmsage.domain.service;

import com.sleekydz86.kmsage.domain.dto.MessageSendResult;
import com.sleekydz86.kmsage.domain.dto.SolapiMessageRequest;

public interface MessageSender {
    MessageSendResult sendMessage(SolapiMessageRequest request);
    boolean supports(String messageType);
}