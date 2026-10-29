package com.sleekydevz86.tts.global.util;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AudioFileNameNormalizer {

    public String normalize(String raw) {
        String base = raw == null ? "" : raw.trim().replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        if (base.toLowerCase(Locale.ROOT).endsWith(".mp3")) {
            base = base.substring(0, base.length() - 4);
        }
        base = base.replaceAll("[^\\p{L}\\p{N}_-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isBlank()) {
            base = "speech";
        }
        if (base.length() > 80) {
            base = base.substring(0, 80).replaceAll("_+$", "");
        }
        return base + ".mp3";
    }
}
