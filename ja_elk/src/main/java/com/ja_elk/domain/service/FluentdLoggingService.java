package com.ja_elk.domain.service;

import org.fluentd.logger.FluentLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FluentdLoggingService {

    @Autowired
    private FluentLogger fluentLogger;

    public void logInfo(String message, String className) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", "INFO");
        data.put("message", message);
        data.put("logger", className);
        data.put("timestamp", System.currentTimeMillis());
        data.put("application", "ja_elk");

        fluentLogger.log("app", data);
    }

    public void logError(String message, String className, Exception e) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", "ERROR");
        data.put("message", message);
        data.put("logger", className);
        data.put("error", e != null ? e.getMessage() : "");
        data.put("timestamp", System.currentTimeMillis());
        data.put("application", "ja_elk");

        fluentLogger.log("app", data);
    }

    public void logWarn(String message, String className) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", "WARN");
        data.put("message", message);
        data.put("logger", className);
        data.put("timestamp", System.currentTimeMillis());
        data.put("application", "ja_elk");

        fluentLogger.log("app", data);
    }
}