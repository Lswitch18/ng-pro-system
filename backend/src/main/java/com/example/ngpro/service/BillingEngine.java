package com.example.ngpro.service;

import com.example.ngpro.model.Customer;
import com.example.ngpro.model.Invoice;
import com.example.ngpro.model.Plan;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.repository.PlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BillingEngine {

    @Autowired private LockManager lockManager;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private PlanRepository planRepo;
    @Autowired private InvoiceRepository invoiceRepo;
    @Autowired private IntegrationHub integrationHub;
    @Autowired private CollectionService collectionService;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Scheduled(cron = "0 0 1 * * ?") // Diário às 1AM
    public void runBillingProcess() {
        String processId = "BATCH_BILLING_" + System.currentTimeMillis();
        if (!lockManager.acquireLock("GLOBAL_BILLING_RUN", processId)) {
            log.error("ABORTING: Another billing process is already running in the cluster!");
            return;
        }
        try {
            int count = executeRun(processId);
            log.info("COMPLETED Enterprise Billing Run: {} | Invoices: {}", processId, count);
        } finally {
            lockManager.releaseLock("GLOBAL_BILLING_RUN", processId);
        }
    }

    public int runManualBilling() {
        String processId = "MANUAL_BILLING_" + System.currentTimeMillis();
        if (!lockManager.acquireLock("GLOBAL_BILLING_RUN", processId)) {
            log.warn("Manual billing blocked — another run in progress");
            return 0;
        }
        try {
            return executeRun(processId);
        } finally {
            lockManager.releaseLock("GLOBAL_BILLING_RUN", processId);
        }
    }

    private int executeRun(String processId) {
        String refMonth = LocalDateTime.now().format(MONTH_FMT);
        List<Customer> customers = customerRepo.findAll();
        int count = 0;
        for (Customer customer : customers) {
            if (!"ACTIVE".equals(customer.getStatus())) continue;
            try {
                Invoice inv = calculateInvoice(customer, refMonth, processId);
                inv = invoiceRepo.save(inv);
                
                // Emite a cobrança (Pix/Boleto) logo após faturar
                collectionService.emitCollection(inv);
                
                integrationHub.syncToSAP(customer.getEmail(), inv.getTotalAmount());
                count++;
            } catch (Exception e) {
                log.error("Error billing customer [{}]: {}", customer.getId(), e.getMessage());
            }
        }
        return count;
    }

    private Invoice calculateInvoice(Customer customer, String refMonth, String processId) {
        Invoice inv = new Invoice();
        inv.setCustomerId(customer.getId());
        inv.setReferenceMonth(refMonth);
        inv.setBillingProcessId(processId);
        inv.setDueDate(LocalDateTime.now().plusDays(10));

        double basePrice = 0.0;
        double overage = 0.0;

        Optional<Plan> planOpt = customer.getPlanId() != null
                ? planRepo.findById(customer.getPlanId()) : Optional.empty();

        if (planOpt.isPresent()) {
            Plan plan = planOpt.get();
            inv.setPlanId(plan.getId());
            basePrice = plan.getBasePrice();

            // Tiered pricing: se exceder tier1, cobra por MB excedente
            double usageMb = customer.getMonthlyUsageMb();
            if (plan.getTier1LimitMb() > 0 && usageMb > plan.getTier1LimitMb()) {
                double excessMb = usageMb - plan.getTier1LimitMb();
                overage = excessMb * plan.getTier2PricePerMb();
            }
        } else {
            basePrice = 89.90; // default ISP price
        }

        inv.setBaseAmount(basePrice);
        inv.setOverageAmount(overage);
        inv.setTotalAmount(basePrice + overage);
        inv.setStatus("PENDING");

        log.info("[BILLING] Customer [{}] | Base: R${} | Overage: R${} | Total: R${}",
                customer.getId(), basePrice, overage, basePrice + overage);
        return inv;
    }
}
