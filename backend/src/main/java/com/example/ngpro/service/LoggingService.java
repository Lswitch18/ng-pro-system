package com.example.ngpro.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    public void logApiRequest(String method, String endpoint, String userId) {
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", userId);
        logger.info("API Request - Method: {}, Endpoint: {}", method, endpoint);
    }

    public void logBusinessEvent(String event, String entity, Long entityId, String details) {
        MDC.put("event", event);
        MDC.put("entity", entity);
        MDC.put("entityId", String.valueOf(entityId));
        logger.info("Business Event: {} on {}#{} - {}", event, entity, entityId, details);
    }

    public void logError(String operation, Exception ex) {
        MDC.put("error", ex.getClass().getSimpleName());
        logger.error("Error in {}: {}", operation, ex.getMessage(), ex);
    }

    public void logPerformance(String operation, long durationMs) {
        MDC.put("durationMs", String.valueOf(durationMs));
        logger.info("Performance: {} took {}ms", operation, durationMs);
    }

    public void clear() {
        MDC.clear();
    }
}
