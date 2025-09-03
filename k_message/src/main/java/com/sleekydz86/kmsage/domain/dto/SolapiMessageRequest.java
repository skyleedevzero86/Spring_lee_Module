package com.sleekydz86.kmsage.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SolapiMessageRequest {
    private String from;
    private String to;
    private String text;
    private String subject;
    private String imageId;
    private String country;
    private LocalDateTime scheduledDate;
    private MessageType messageType;
    private KakaoMessageOptions kakaoOptions;
    private List<String> toList;

    public static class KakaoMessageOptions {
        private String pfId;
        private String templateId;
        private boolean disableSms;
        private Map<String, String> variables;
        private List<KakaoButton> buttons;

        public static class KakaoButton {
            private String name;
            private String type;
            private String urlMo;
            private String urlPc;
            private String schemeIos;
            private String schemeAndroid;

            public KakaoButton() {}

            public KakaoButton(String name, String type, String urlMo, String urlPc,
                               String schemeIos, String schemeAndroid) {
                this.name = name;
                this.type = type;
                this.urlMo = urlMo;
                this.urlPc = urlPc;
                this.schemeIos = schemeIos;
                this.schemeAndroid = schemeAndroid;
            }

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public String getUrlMo() { return urlMo; }
            public void setUrlMo(String urlMo) { this.urlMo = urlMo; }
            public String getUrlPc() { return urlPc; }
            public void setUrlPc(String urlPc) { this.urlPc = urlPc; }
            public String getSchemeIos() { return schemeIos; }
            public void setSchemeIos(String schemeIos) { this.schemeIos = schemeIos; }
            public String getSchemeAndroid() { return schemeAndroid; }
            public void setSchemeAndroid(String schemeAndroid) { this.schemeAndroid = schemeAndroid; }
        }

        public String getPfId() { return pfId; }
        public void setPfId(String pfId) { this.pfId = pfId; }
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        public boolean isDisableSms() { return disableSms; }
        public void setDisableSms(boolean disableSms) { this.disableSms = disableSms; }
        public Map<String, String> getVariables() { return variables; }
        public void setVariables(Map<String, String> variables) { this.variables = variables; }
        public List<KakaoButton> getButtons() { return buttons; }
        public void setButtons(List<KakaoButton> buttons) { this.buttons = buttons; }
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public KakaoMessageOptions getKakaoOptions() { return kakaoOptions; }
    public void setKakaoOptions(KakaoMessageOptions kakaoOptions) { this.kakaoOptions = kakaoOptions; }
    public List<String> getToList() { return toList; }
    public void setToList(List<String> toList) { this.toList = toList; }
}