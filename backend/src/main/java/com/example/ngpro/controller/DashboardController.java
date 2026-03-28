package com.example.ngpro.controller;

import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.repository.PlanRepository;
import com.example.ngpro.service.LockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private CustomerRepository customerRepo;
    @Autowired private InvoiceRepository invoiceRepo;
    @Autowired private PlanRepository planRepo;
    @Autowired private LockManager lockManager;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customerRepo.count());
        stats.put("activeCustomers", customerRepo.countByStatus("ACTIVE"));
        stats.put("suspendedCustomers", customerRepo.countByStatus("SUSPENDED"));
        stats.put("blockedCustomers", customerRepo.countByStatus("BLOCKED"));

        Double paidRevenue = invoiceRepo.sumPaidRevenue();
        Double pendingRevenue = invoiceRepo.sumPendingRevenue();

        stats.put("paidRevenue", paidRevenue != null ? paidRevenue : 0.0);
        stats.put("pendingRevenue", pendingRevenue != null ? pendingRevenue : 0.0);
        stats.put("pendingInvoices", invoiceRepo.findByStatus("PENDING").size());
        stats.put("overdueInvoices", invoiceRepo.findByStatus("OVERDUE").size());
        
        stats.put("totalPlans", planRepo.count());
        stats.put("lockserverStatus", lockManager.getActiveLockCount() > 0 ? "BUSY" : "RUNNING");
        stats.put("lockserverActiveLocks", lockManager.getActiveLockCount());
        return stats;
    }
}
