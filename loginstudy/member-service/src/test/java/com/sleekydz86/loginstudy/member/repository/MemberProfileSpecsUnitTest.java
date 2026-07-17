package com.sleekydz86.loginstudy.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberProfileSpecsUnitTest {

	@Test
	@DisplayName("LIKE 이스케이프가 SQL 와일드카드 문자를 이스케이프한다")
	void escapeLikeEscapesSqlWildcardCharacters() {
		// given
		String raw = "a%b_c\\d";

		// when
		String escaped = MemberProfileSpecs.escapeLike(raw);

		// then
		assertThat(escaped).isEqualTo("a\\%b\\_c\\\\d");
	}
}
