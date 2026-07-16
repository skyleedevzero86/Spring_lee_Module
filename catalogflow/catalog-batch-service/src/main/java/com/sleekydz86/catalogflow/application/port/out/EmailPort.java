package com.sleekydz86.catalogflow.application.port.out;

public interface EmailPort {

	void send(String to, String subject, String htmlBody);
}
