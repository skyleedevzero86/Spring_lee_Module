package com.sleekydz86.patternlab.number.adapter.in.web;

import java.util.List;

import com.sleekydz86.patternlab.number.domain.NumberAnalysis;
import com.sleekydz86.patternlab.number.domain.PatternMatch;

public final class NumberWebModels {

	private NumberWebModels() {
	}

	public record AnalyzeRequest(String value) {
	}

	public record MatchView(String type, String category, boolean matched) {

		static MatchView from(PatternMatch match) {
			return new MatchView(match.target().typeName(), match.target().category(), match.matched());
		}
	}

	public record AnalyzeResponse(
		String value,
		String detectedType,
		String primaryCategory,
		List<MatchView> matches
	) {

		public static AnalyzeResponse from(NumberAnalysis analysis) {
			return new AnalyzeResponse(
				analysis.rawValue(),
				analysis.detectedType().label(),
				analysis.primaryCategory(),
				analysis.matches().stream().map(MatchView::from).toList()
			);
		}
	}
}
