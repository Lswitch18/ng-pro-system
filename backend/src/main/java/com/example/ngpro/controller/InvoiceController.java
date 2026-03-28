package com.example.ngpro.controller;

import com.example.ngpro.model.Customer;
import com.example.ngpro.model.Invoice;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.service.BillingEngine;
import com.example.ngpro.service.CollectionService;
import com.example.ngpro.service.DunningService;
import com.example.ngpro.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@Validated
public class InvoiceController {

    @Autowired private InvoiceRepository invoiceRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private BillingEngine billingEngine;
    @Autowired private CollectionService collectionService;
    @Autowired private DunningService dunningService;
    @Autowired private EmailService emailService;

    @GetMapping
    public Page<Invoice> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return invoiceRepo.findAll(pageable);
    }

    @GetMapping("/customer/{customerId}")
    public List<Invoice> getByCustomer(@PathVariable Long customerId) {
        return invoiceRepo.findByCustomerId(customerId);
    }

    @GetMapping("/status/{status}")
    public Page<Invoice> getByStatus(@PathVariable String status, @PageableDefault(size = 20) Pageable pageable) {
        return invoiceRepo.findByStatus(status.toUpperCase(), pageable);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> pay(@PathVariable Long id) {
        if (!invoiceRepo.existsById(id)) return ResponseEntity.notFound().build();
        collectionService.processConciliation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/run-billing")
    public ResponseEntity<Map<String, Object>> runBilling() {
        int generated = billingEngine.runManualBilling();
        return ResponseEntity.ok(Map.of(
            "message", "Billing run concluído",
            "invoicesGenerated", generated
        ));
    }

    @PostMapping("/run-dunning")
    public ResponseEntity<Map<String, Object>> runDunning() {
        dunningService.runDunningProcess();
        return ResponseEntity.ok(Map.of(
            "message", "Régua de cobrança (Dunning) executada"
        ));
    }

    @GetMapping("/overdue-customers")
    public List<Map<String, Object>> getOverdueCustomers(@RequestParam(required = false) String status) {
        List<Invoice> overdueInvoices;
        
        if (status != null && !status.isEmpty()) {
            overdueInvoices = invoiceRepo.findByStatus(status.toUpperCase());
        } else {
            overdueInvoices = invoiceRepo.findByStatus("OVERDUE");
        }
        
        Set<Long> processedCustomers = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Invoice invoice : overdueInvoices) {
            if (processedCustomers.contains(invoice.getCustomerId())) continue;
            
            Optional<Customer> customerOpt = customerRepo.findById(invoice.getCustomerId());
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                processedCustomers.add(customer.getId());
                
                List<Invoice> customerInvoices = invoiceRepo.findByCustomerId(customer.getId());
                double totalDebt = customerInvoices.stream()
                    .filter(i -> "OVERDUE".equals(i.getStatus()))
                    .mapToDouble(Invoice::getTotalAmount)
                    .sum();
                
                Map<String, Object> item = new HashMap<>();
                item.put("id", customer.getId());
                item.put("name", customer.getName());
                item.put("email", customer.getEmail());
                item.put("phone", customer.getPhone());
                item.put("status", customer.getStatus());
                item.put("totalDebt", totalDebt);
                item.put("overdueInvoices", customerInvoices.stream()
                    .filter(i -> "OVERDUE".equals(i.getStatus()))
                    .map(i -> Map.of(
                        "id", i.getId(),
                        "amount", i.getTotalAmount(),
                        "dueDate", i.getDueDate() != null ? i.getDueDate().toString() : null,
                        "referenceMonth", i.getReferenceMonth()
                    ))
                    .collect(Collectors.toList()));
                result.add(item);
            }
        }
        return result;
    }

    @PostMapping("/send-overdue-notifications")
    public ResponseEntity<Map<String, Object>> sendOverdueNotifications(@RequestBody Map<String, Object> body) {
        List<Integer> customerIds = (List<Integer>) body.get("customerIds");
        String action = (String) body.getOrDefault("action", "email");
        
        int sent = 0;
        for (Integer id : customerIds) {
            Optional<Customer> customerOpt = customerRepo.findById(id.longValue());
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                List<Invoice> overdueInvoices = invoiceRepo.findByCustomerId(id.longValue()).stream()
                    .filter(i -> "OVERDUE".equals(i.getStatus()))
                    .collect(Collectors.toList());
                
                double totalDebt = overdueInvoices.stream()
                    .mapToDouble(Invoice::getTotalAmount)
                    .sum();
                
                if ("email".equals(action) && customer.getEmail() != null) {
                    emailService.sendCollectionEmail(customer.getEmail(), customer.getName(), totalDebt, 
                        overdueInvoices.isEmpty() ? "" : overdueInvoices.get(0).getReferenceMonth());
                    sent++;
                } else if ("signal".equals(action)) {
                    emailService.sendCollectionEmail(customer.getEmail(), customer.getName(), totalDebt,
                        overdueInvoices.isEmpty() ? "" : overdueInvoices.get(0).getReferenceMonth());
                    sent++;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "sent", sent,
            "message", sent + " notificações/envios realizados"
        ));
    }

    @PostMapping("/suspend-overdue")
    public ResponseEntity<Map<String, Object>> suspendOverdueCustomers(@RequestBody Map<String, Object> body) {
        List<Integer> customerIds = (List<Integer>) body.get("customerIds");
        
        int suspended = 0;
        for (Integer id : customerIds) {
            Optional<Customer> customerOpt = customerRepo.findById(id.longValue());
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                customer.setStatus("SUSPENDED");
                customerRepo.save(customer);
                suspended++;
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "suspended", suspended,
            "message", suspended + " clientes suspensos"
        ));
    }
}
