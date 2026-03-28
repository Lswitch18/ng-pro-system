package com.example.ngpro.service;

import com.example.ngpro.model.Invoice;
import com.example.ngpro.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DunningService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private LockManager lockManager;

    // Régua de Cobrança: Roda a cada hora para checar vencimentos
    @Scheduled(cron = "0 0 * * * ?")
    public void runDunningProcess() {
        String processId = "DUNNING_" + System.currentTimeMillis();
        
        if (!lockManager.acquireLock("GLOBAL_DUNNING_RUN", processId)) {
            return;
        }

        try {
            log.info("[DUNNING] Starting Collection Rule check...");
            List<Invoice> pendingInvoices = invoiceRepository.findByStatus("PENDING");
            
            LocalDateTime now = LocalDateTime.now();
            
            for (Invoice inv : pendingInvoices) {
                if (inv.getDueDate().isBefore(now.minusDays(5))) {
                    log.warn("[DUNNING] Invoice [{}] overdue by 5+ days. Triggering SUSPENSION for Customer [{}]", 
                            inv.getId(), inv.getCustomerId());
                    inv.setStatus("OVERDUE");
                    invoiceRepository.save(inv);
                    provisioningService.suspendService(inv.getCustomerId());
                } else if (inv.getDueDate().isBefore(now)) {
                    log.info("[DUNNING] Invoice [{}] overdue. Status marked as OVERDUE", inv.getId());
                    inv.setStatus("OVERDUE");
                    invoiceRepository.save(inv);
                }
            }
        } finally {
            lockManager.releaseLock("GLOBAL_DUNNING_RUN", processId);
        }
    }
}
