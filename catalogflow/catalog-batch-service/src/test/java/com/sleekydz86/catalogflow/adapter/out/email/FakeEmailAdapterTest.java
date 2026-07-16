package com.sleekydz86.catalogflow.adapter.out.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FakeEmailAdapterTest {

	@Test
	@DisplayName("가짜 이메일 어댑터는 발송 내용을 기록한다")
	void shouldRecordSentMail() {
		// given
		FakeEmailAdapter adapter = new FakeEmailAdapter();

		// when
		adapter.send("ops@test.local", "일일 리포트", "<p>내용</p>");

		// then
		assertEquals(1, adapter.getSentMails().size());
		assertEquals("ops@test.local", adapter.getSentMails().getFirst().to());
		assertEquals("일일 리포트", adapter.getSentMails().getFirst().subject());
	}
}
