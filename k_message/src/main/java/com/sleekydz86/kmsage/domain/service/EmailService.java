package com.sleekydz86.kmsage.domain.service;

import com.sleekydz86.kmsage.domain.dto.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public boolean sendSimpleEmail(EmailRequest emailRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(emailRequest.getTo());

            if (emailRequest.getCc() != null && !emailRequest.getCc().trim().isEmpty()) {
                message.setCc(emailRequest.getCc());
            }

            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getContent());

            mailSender.send(message);
            return true;

        } catch (MailException e) {
            System.err.println("이메일 발송 실패: " + e.getMessage());
            return false;
        }
    }

    public boolean sendHtmlEmail(EmailRequest emailRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());

            if (emailRequest.getCc() != null && !emailRequest.getCc().trim().isEmpty()) {
                helper.setCc(emailRequest.getCc());
            }

            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getContent(), emailRequest.isHtml());

            mailSender.send(message);
            return true;

        } catch (MessagingException | MailException e) {
            System.err.println("HTML 이메일 발송 실패: " + e.getMessage());
            return false;
        }
    }

    public boolean sendNotificationEmail(String to, String kakaoMessageType, boolean success) {
        try {
            String subject = "[카카오 메시지] 발송 " + (success ? "성공" : "실패") + " 알림";
            String content = createNotificationContent(kakaoMessageType, success);

            EmailRequest emailRequest = new EmailRequest(to, subject, content);
            emailRequest.setHtml(true);

            return sendHtmlEmail(emailRequest);

        } catch (Exception e) {
            System.err.println("알림 이메일 발송 실패: " + e.getMessage());
            return false;
        }
    }

    private String createNotificationContent(String messageType, boolean success) {
        String status = success ? "✅ 성공" : "❌ 실패";
        String statusColor = success ? "#28a745" : "#dc3545";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f8f9fa; }
                        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        .header { background: #FEE500; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 30px; }
                        .status { font-size: 24px; font-weight: bold; color: %s; margin: 20px 0; }
                        .footer { background: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #6c757d; border-radius: 0 0 8px 8px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>🚀 카카오 메시지 발송 알림</h2>
                        </div>
                        <div class="content">
                            <p><strong>메시지 유형:</strong> %s</p>
                            <p><strong>발송 상태:</strong> <span class="status">%s</span></p>
                            <p><strong>발송 시간:</strong> %s</p>
                            <hr>
                            <p>카카오톡의 '나와의 채팅'에서 메시지를 확인해보세요!</p>
                        </div>
                        <div class="footer">
                            <p>이 메시지는 카카오 메시지 발송 시스템에서 자동으로 발송되었습니다.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(statusColor, messageType, status, java.time.LocalDateTime.now().toString());
    }
}