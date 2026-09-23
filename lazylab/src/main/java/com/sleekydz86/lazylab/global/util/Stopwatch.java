package com.sleekydz86.lazylab.global.util;

public final class Stopwatch {

	private final long startedAtNanos;

	private Stopwatch(long startedAtNanos) {
		this.startedAtNanos = startedAtNanos;
	}

	public static Stopwatch start() {
		return new Stopwatch(System.nanoTime());
	}

	public long elapsedMillis() {
		return (System.nanoTime() - startedAtNanos) / 1_000_000L;
	}
}
