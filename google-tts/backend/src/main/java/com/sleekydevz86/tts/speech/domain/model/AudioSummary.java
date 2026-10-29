package com.sleekydevz86.tts.speech.domain.model;

import java.time.Instant;

public record AudioSummary(String fileName, long size, Instant modifiedAt) {
}
