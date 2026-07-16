package com.sleekydz86.catalogflow.adapter.out.email;

import com.sleekydz86.catalogflow.application.port.out.EmailPort;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.batch.email-provider", havingValue = "smtp")
public class SpringMailEmailAdapter implements EmailPort {

	private final JavaMailSender javaMailSender;

	public SpringMailEmailAdapter(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	@Override
	public void send(String to, String subject, String htmlBody) {
		try {
			var message = javaMailSender.createMimeMessage();
			var helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);
			javaMailSender.send(message);
		}
		catch (Exception exception) {
			throw new ApplicationException("이메일 발송에 실패했습니다", exception);
		}
	}
}
