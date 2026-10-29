package com.example.tts.speech.domain.model;

import java.time.Instant;

public record AudioSummary(String fileName, long size, Instant modifiedAt) {
}
