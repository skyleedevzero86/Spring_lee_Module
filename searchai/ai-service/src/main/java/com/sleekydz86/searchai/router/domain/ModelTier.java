package com.sleekydz86.searchai.router.domain;

public enum ModelTier {

	LUNA("간단한 요청: 인사, 한 줄 사실, 짧은 포맷팅, 짧은 다시쓰기."),
	TERRA("일상 작업: 요약, 개념 설명, 짧은 함수 작성, 명확한 질문."),
	SOL("복잡한 전문 작업: 다단계 추론, 여러 파일 코드, 상세 분석."),
	ASTRA("가장 어려운 문제: 심층 리서치, 시스템 아키텍처, 형식 증명.");

	private final String description;

	ModelTier(String description) {
		this.description = description;
	}

	public String description() {
		return description;
	}

	public String displayName() {
		return name().toLowerCase();
	}

	public ModelTier upgrade() {
		return switch (this) {
			case LUNA -> TERRA;
			case TERRA -> SOL;
			case SOL, ASTRA -> ASTRA;
		};
	}

	public ModelTier downgrade() {
		return switch (this) {
			case ASTRA -> SOL;
			case SOL -> TERRA;
			case TERRA, LUNA -> LUNA;
		};
	}
}
