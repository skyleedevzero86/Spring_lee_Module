package com.example.tts.speech.adapter.in.web;

import java.time.Instant;

public record SavedAudioResponse(String filename, long size, Instant modifiedAt) {
}
