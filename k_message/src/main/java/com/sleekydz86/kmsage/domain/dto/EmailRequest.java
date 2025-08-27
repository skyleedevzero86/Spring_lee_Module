package com.sleekydz86.kmsage.domain.dto;

public class EmailRequest {
    private String to;
    private String cc;
    private String subject;
    private String content;
    private boolean isHtml;

    public EmailRequest() {}

    public EmailRequest(String to, String subject, String content) {
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.isHtml = false;
    }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getCc() { return cc; }
    public void setCc(String cc) { this.cc = cc; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isHtml() { return isHtml; }
    public void setHtml(boolean html) { isHtml = html; }
}