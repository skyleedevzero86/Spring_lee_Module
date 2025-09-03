package com.sleekydz86.kmsage.domain.service;

import com.sleekydz86.kmsage.domain.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UnifiedMessageService {

    @Autowired
    private MessageSenderFactory messageSenderFactory;

    @Autowired
    private SolapiMessageService solapiMessageService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KakaoService kakaoService;

    public MessageSendResult sendSms(SolapiMessageRequest request) {
        request.setMessageType(MessageType.SMS);
        return messageSenderFactory.getMessageSender(MessageType.SMS.name()).sendMessage(request);
    }

    public MessageSendResult sendLms(SolapiMessageRequest request) {
        request.setMessageType(MessageType.LMS);
        return messageSenderFactory.getMessageSender(MessageType.LMS.name()).sendMessage(request);
    }

    public MessageSendResult sendMms(SolapiMessageRequest request) {
        request.setMessageType(MessageType.MMS);
        return messageSenderFactory.getMessageSender(MessageType.MMS.name()).sendMessage(request);
    }

    public MessageSendResult sendKakaoAlimtalk(SolapiMessageRequest request) {
        request.setMessageType(MessageType.KAKAO_ALIMTALK);
        return messageSenderFactory.getMessageSender(MessageType.KAKAO_ALIMTALK.name()).sendMessage(request);
    }

    public MessageSendResult sendKakaoFriendtalk(SolapiMessageRequest request) {
        request.setMessageType(MessageType.KAKAO_FRIENDTALK);
        return messageSenderFactory.getMessageSender(MessageType.KAKAO_FRIENDTALK.name()).sendMessage(request);
    }

    public String uploadImage(MultipartFile file) throws IOException {
        return solapiMessageService.uploadImage(file);
    }

    public String uploadKakaoImage(MultipartFile file, String link) throws IOException {
        return solapiMessageService.uploadKakaoImage(file, link);
    }

    public String getBalance() {
        return solapiMessageService.getBalance();
    }

    public UnifiedSendResult sendAllChannels(UnifiedSendRequest request) {
        UnifiedSendResult result = new UnifiedSendResult();

        if (request.getSolapiRequest() != null) {
            MessageSendResult solapiResult = messageSenderFactory
                    .getMessageSender(request.getSolapiRequest().getMessageType().name())
                    .sendMessage(request.getSolapiRequest());
            result.setSolapiResult(solapiResult);
        }

        if (request.getEmailRequest() != null) {
            boolean emailSuccess = emailService.sendSimpleEmail(request.getEmailRequest());
            result.setEmailResult(emailSuccess ? "성공" : "실패");
        }

        if (request.getKakaoTemplate() != null && request.getAccessToken() != null) {
            boolean kakaoSuccess = kakaoService.sendMessageToMe(request.getAccessToken(), request.getKakaoTemplate());
            result.setKakaoResult(kakaoSuccess ? "성공" : "실패");
        }

        return result;
    }

    public static class UnifiedSendRequest {
        private SolapiMessageRequest solapiRequest;
        private EmailRequest emailRequest;
        private MessageTemplate kakaoTemplate;
        private String accessToken;

        public SolapiMessageRequest getSolapiRequest() {
            return solapiRequest;
        }

        public void setSolapiRequest(SolapiMessageRequest solapiRequest) {
            this.solapiRequest = solapiRequest;
        }

        public EmailRequest getEmailRequest() {
            return emailRequest;
        }

        public void setEmailRequest(EmailRequest emailRequest) {
            this.emailRequest = emailRequest;
        }

        public MessageTemplate getKakaoTemplate() {
            return kakaoTemplate;
        }

        public void setKakaoTemplate(MessageTemplate kakaoTemplate) {
            this.kakaoTemplate = kakaoTemplate;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    public static class UnifiedSendResult {
        private MessageSendResult solapiResult;
        private String emailResult;
        private String kakaoResult;
        private boolean overallSuccess;

        public MessageSendResult getSolapiResult() {
            return solapiResult;
        }

        public void setSolapiResult(MessageSendResult solapiResult) {
            this.solapiResult = solapiResult;
        }

        public String getEmailResult() {
            return emailResult;
        }

        public void setEmailResult(String emailResult) {
            this.emailResult = emailResult;
        }

        public String getKakaoResult() {
            return kakaoResult;
        }

        public void setKakaoResult(String kakaoResult) {
            this.kakaoResult = kakaoResult;
        }

        public boolean isOverallSuccess() {
            return overallSuccess;
        }

        public void setOverallSuccess(boolean overallSuccess) {
            this.overallSuccess = overallSuccess;
        }
    }
}
