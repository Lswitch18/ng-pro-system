package com.example.ngpro.service;

import com.example.ngpro.model.UsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsageProcessor {

    @Async
    @EventListener
    public void handleUsageEvent(UsageEvent event) {
        log.debug("Processing USAGE EVENT for Customer [{}]: {} of {}", 
                event.getCustomerId(), event.getAmount(), event.getResourceType());
        
        // Simulação de processamento complexo / agregação
        try {
            Thread.sleep(100); // Simulando latência de I/O
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Usage EVENT PERSISTED for Customer [{}]", event.getCustomerId());
    }
}
