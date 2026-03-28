package com.example.ngpro.controller;

import com.example.ngpro.model.AuditLog;
import com.example.ngpro.model.Invoice;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.repository.PlanRepository;
import com.example.ngpro.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@Slf4j
public class ReportController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private AuditService auditService;

    @GetMapping("/financial/summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(i -> i.getCreatedAt() != null && 
                           !i.getCreatedAt().isBefore(startDateTime) && 
                           !i.getCreatedAt().isAfter(endDateTime))
                .toList();

        double totalInvoiced = invoices.stream().mapToDouble(Invoice::getTotalAmount).sum();
        double totalPaid = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .mapToDouble(Invoice::getTotalAmount).sum();
        double totalPending = invoices.stream()
                .filter(i -> "PENDING".equals(i.getStatus()))
                .mapToDouble(Invoice::getTotalAmount).sum();
        double totalOverdue = invoices.stream()
                .filter(i -> "OVERDUE".equals(i.getStatus()))
                .mapToDouble(Invoice::getTotalAmount).sum();

        long paidCount = invoices.stream().filter(i -> "PAID".equals(i.getStatus())).count();
        long pendingCount = invoices.stream().filter(i -> "PENDING".equals(i.getStatus())).count();
        long overdueCount = invoices.stream().filter(i -> "OVERDUE".equals(i.getStatus())).count();

        double avgTicket = paidCount > 0 ? totalPaid / paidCount : 0;
        double collectionRate = totalInvoiced > 0 ? (totalPaid / totalInvoiced) * 100 : 0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("period", Map.of("start", startDate.toString(), "end", endDate.toString()));
        summary.put("invoices", Map.of(
                "total", invoices.size(),
                "paid", paidCount,
                "pending", pendingCount,
                "overdue", overdueCount
        ));
        summary.put("revenue", Map.of(
                "totalInvoiced", totalInvoiced,
                "totalReceived", totalPaid,
                "totalPending", totalPending,
                "totalOverdue", totalOverdue,
                "averageTicket", avgTicket,
                "collectionRate", collectionRate
        ));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/financial/monthly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(
            @RequestParam(defaultValue = "12") int months) {
        
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        for (int i = 0; i < months; i++) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

            List<Invoice> monthInvoices = invoiceRepository.findAll().stream()
                    .filter(i -> i.getCreatedAt() != null && 
                               !i.getCreatedAt().isBefore(start) && 
                               !i.getCreatedAt().isAfter(end))
                    .toList();

            double total = monthInvoices.stream().mapToDouble(Invoice::getTotalAmount).sum();
            double paid = monthInvoices.stream()
                    .filter(i -> "PAID".equals(i.getStatus()))
                    .mapToDouble(Invoice::getTotalAmount).sum();

            Map<String, Object> month = new LinkedHashMap<>();
            month.put("month", ym.toString());
            month.put("invoiced", total);
            month.put("received", paid);
            month.put("invoices", monthInvoices.size());
            monthlyData.add(month);
        }

        return ResponseEntity.ok(monthlyData);
    }

    @GetMapping("/customers/by-plan")
    public ResponseEntity<List<Map<String, Object>>> getCustomersByPlan() {
        return ResponseEntity.ok(List.of(
                Map.of("plan", "100Mbps", "customers", 2, "revenue", 159.80),
                Map.of("plan", "500Mbps", "customers", 3, "revenue", 389.70),
                Map.of("plan", "1Gbps", "customers", 2, "revenue", 399.80),
                Map.of("plan", "10Gbps", "customers", 1, "revenue", 349.90)
        ));
    }

    @GetMapping("/customers/debtors")
    public ResponseEntity<List<Map<String, Object>>> getDebtors() {
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus("OVERDUE");
        
        Map<Long, Double> customerDebt = new HashMap<>();
        for (Invoice inv : overdueInvoices) {
            customerDebt.merge(inv.getCustomerId(), inv.getTotalAmount(), Double::sum);
        }

        List<Map<String, Object>> debtors = new ArrayList<>();
        customerDebt.forEach((customerId, debt) -> {
            customerRepository.findById(customerId).ifPresent(c -> {
                Map<String, Object> debtor = new LinkedHashMap<>();
                debtor.put("customerId", customerId);
                debtor.put("name", c.getName());
                debtor.put("email", c.getEmail());
                debtor.put("phone", c.getPhone());
                debtor.put("totalDebt", debt);
                debtors.add(debtor);
            });
        });

        debtors.sort((a, b) -> Double.compare((Double) b.get("totalDebt"), (Double) a.get("totalDebt")));
        return ResponseEntity.ok(debtors);
    }

    @GetMapping("/audit/logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        
        if (entityType != null && entityId != null) {
            return ResponseEntity.ok(Map.of(
                    "data", auditService.getAuditLogsByEntity(entityType, entityId),
                    "total", 0
            ));
        }
        
        return ResponseEntity.ok(Map.of(
                "data", auditService.getAuditLogs(page, size).getContent(),
                "total", auditService.getAuditLogs(page, size).getTotalElements()
        ));
    }

    @GetMapping("/kpi/dashboard")
    public ResponseEntity<Map<String, Object>> getKpiDashboard() {
        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.findAll().stream()
                .filter(c -> "ACTIVE".equals(c.getStatus())).count();
        long suspendedCustomers = customerRepository.findAll().stream()
                .filter(c -> "SUSPENDED".equals(c.getStatus())).count();
        long blockedCustomers = customerRepository.findAll().stream()
                .filter(c -> "BLOCKED".equals(c.getStatus())).count();

        List<Invoice> invoices = invoiceRepository.findAll();
        double monthRevenue = invoices.stream()
                .filter(i -> i.getPaidAt() != null && 
                           i.getPaidAt().getMonth() == LocalDateTime.now().getMonth())
                .mapToDouble(Invoice::getTotalAmount).sum();

        double overdueAmount = invoices.stream()
                .filter(i -> "OVERDUE".equals(i.getStatus()))
                .mapToDouble(Invoice::getTotalAmount).sum();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("customers", Map.of(
                "total", totalCustomers,
                "active", activeCustomers,
                "suspended", suspendedCustomers,
                "blocked", blockedCustomers
        ));
        kpi.put("revenue", Map.of(
                "month", monthRevenue,
                "overdue", overdueAmount,
                "churnRate", totalCustomers > 0 ? (blockedCustomers * 100.0 / totalCustomers) : 0
        ));
        kpi.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(kpi);
    }
}
