package com.sleekydz86.patternlab.number.domain;

public enum DetectedType {

	INTEGER,
	LONG,
	FLOAT,
	DOUBLE;

	public String label() {
		return switch (this) {
			case INTEGER -> "INTEGER";
			case LONG -> "LONG";
			case FLOAT -> "FLOAT";
			case DOUBLE -> "DOUBLE";
		};
	}
}
