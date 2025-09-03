package com.sleekydz86.kmsage.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MessageSendResult {
    private boolean success;
    private String message;
    private String messageId;
    private LocalDateTime sentAt;
    private MessageType messageType;
    private String from;
    private String to;
    private List<String> failedNumbers;
    private String errorCode;
    private String errorMessage;

    public MessageSendResult() {}

    public MessageSendResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    public static MessageSendResult success(String messageId, MessageType messageType, String from, String to) {
        MessageSendResult result = new MessageSendResult(true, "메시지 발송 성공");
        result.setMessageId(messageId);
        result.setMessageType(messageType);
        result.setFrom(from);
        result.setTo(to);
        return result;
    }

    public static MessageSendResult failure(String errorMessage, String errorCode) {
        MessageSendResult result = new MessageSendResult(false, "메시지 발송 실패");
        result.setErrorMessage(errorMessage);
        result.setErrorCode(errorCode);
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public List<String> getFailedNumbers() { return failedNumbers; }
    public void setFailedNumbers(List<String> failedNumbers) { this.failedNumbers = failedNumbers; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}