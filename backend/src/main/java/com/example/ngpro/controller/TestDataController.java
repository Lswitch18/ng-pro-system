package com.example.ngpro.controller;

import com.example.ngpro.model.Customer;
import com.example.ngpro.model.Invoice;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestDataController {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private InvoiceRepository invoiceRepo;

    @Autowired
    private EmailService emailService;

    @PostMapping("/create-delinquent-customers")
    public ResponseEntity<?> createDelinquentCustomers() {
        List<Map<String, String>> customers = List.of(
            Map.of("email", "tommsanje@gmail.com", "name", "Tommsanje Silva", "cpf", "123.456.789-00"),
            Map.of("email", "wellyntonjeronimo@outlook.com", "name", "Wellynton Jeronimo", "cpf", "987.654.321-00")
        );

        for (Map<String, String> c : customers) {
            Customer customer = new Customer();
            customer.setName(c.get("name"));
            customer.setEmail(c.get("email"));
            customer.setCpfCnpj(c.get("cpf"));
            customer.setPhone("11999999999");
            customer.setStatus("SUSPENDED");
            customer.setCity("São Paulo");
            customer.setState("SP");
            customer.setPlanId(1L);
            customer.setMonthlyUsageMb(5000);
            customer.setContractStart(LocalDateTime.now().minusMonths(3));
            
            Customer saved = customerRepo.save(customer);

            Invoice invoice = new Invoice();
            invoice.setCustomerId(saved.getId());
            invoice.setPlanId(1L);
            invoice.setStatus("OVERDUE");
            invoice.setBaseAmount(99.90);
            invoice.setOverageAmount(25.00);
            invoice.setTotalAmount(124.90);
            invoice.setDueDate(LocalDateTime.now().minusDays(15));
            invoice.setReferenceMonth("2026-02");
            invoice.setBillingProcessId("BILL-2026-02");
            
            invoiceRepo.save(invoice);

            emailService.sendCollectionEmail(saved.getEmail(), saved.getName(), 124.90, "2026-02");
        }

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "2 clientes inadimplentes criados com faturas vencidas",
            "mailpit", "http://localhost:8025"
        ));
    }

    @PostMapping("/collection-email")
    public ResponseEntity<?> testCollectionEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String name = body.get("name");
        double amount = body.get("amount") != null ? Double.parseDouble(body.get("amount")) : 99.90;
        String month = body.getOrDefault("month", "2026-03");

        emailService.sendCollectionEmail(to, name, amount, month);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "E-mail de cobrança enviado para " + to
        ));
    }
}
