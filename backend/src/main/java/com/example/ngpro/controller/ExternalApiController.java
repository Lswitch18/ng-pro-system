package com.example.ngpro.controller;

import com.example.ngpro.model.ApiKey;
import com.example.ngpro.model.Customer;
import com.example.ngpro.model.Invoice;
import com.example.ngpro.model.Plan;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.repository.PlanRepository;
import com.example.ngpro.service.ApiKeyService;
import com.example.ngpro.service.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/external")
@Slf4j
public class ExternalApiController {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private CollectionService collectionService;

    private static final String HEADER_API_KEY = "X-API-Key";

    private ResponseEntity<?> authenticate(String apiKey, String requiredPermission) {
        if (apiKey == null || apiKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "API Key não fornecida", "code", "NO_API_KEY"));
        }

        Optional<ApiKey> keyOpt = apiKeyService.validateApiKey(apiKey);
        if (keyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "API Key inválida ou inativa", "code", "INVALID_API_KEY"));
        }

        ApiKey key = keyOpt.get();
        if (!apiKeyService.hasPermission(key, requiredPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Permissão insuficiente: " + requiredPermission, "code", "INSUFFICIENT_PERMISSION"));
        }

        return null;
    }

    @PostMapping("/auth/create-key")
    public ResponseEntity<?> createApiKey(@RequestHeader(HEADER_API_KEY) String apiKey,
                                          @RequestBody Map<String, String> request) {
        ResponseEntity<?> auth = authenticate(apiKey, "apikeys");
        if (auth != null) return auth;

        String clientName = request.get("clientName");
        String clientType = request.get("clientType");
        String permissions = request.get("permissions");

        if (clientName == null || clientType == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "clientName e clientType são obrigatórios"));
        }

        ApiKey newKey = apiKeyService.generateApiKey(clientName, clientType, permissions);
        
        return ResponseEntity.ok(Map.of(
                "apiKey", newKey.getApiKey(),
                "clientName", newKey.getClientName(),
                "clientType", newKey.getClientType(),
                "permissions", newKey.getPermissions(),
                "expiresAt", newKey.getExpiresAt().toString()
        ));
    }

    @GetMapping("/customers")
    public ResponseEntity<?> listCustomers(@RequestHeader(HEADER_API_KEY) String apiKey,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int limit) {
        ResponseEntity<?> auth = authenticate(apiKey, "customers");
        if (auth != null) return auth;

        List<Customer> customers = customerRepository.findAll();
        
        if (status != null) {
            customers = customers.stream()
                    .filter(c -> status.equalsIgnoreCase(c.getStatus()))
                    .toList();
        }

        int start = page * limit;
        int end = Math.min(start + limit, customers.size());
        List<Customer> paged = start < customers.size() ? customers.subList(start, end) : List.of();

        List<Map<String, Object>> result = paged.stream().map(this::customerToMap).toList();

        return ResponseEntity.ok(Map.of(
                "data", result,
                "total", customers.size(),
                "page", page,
                "limit", limit
        ));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomer(@RequestHeader(HEADER_API_KEY) String apiKey,
                                          @PathVariable Long id) {
        ResponseEntity<?> auth = authenticate(apiKey, "customers");
        if (auth != null) return auth;

        return customerRepository.findById(id)
                .map(c -> ResponseEntity.ok(customerToMap(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestHeader(HEADER_API_KEY) String apiKey,
                                              @RequestBody Map<String, Object> request) {
        ResponseEntity<?> auth = authenticate(apiKey, "customers");
        if (auth != null) return auth;

        Customer customer = new Customer();
        customer.setName((String) request.get("name"));
        customer.setEmail((String) request.get("email"));
        customer.setPhone((String) request.get("phone"));
        customer.setCpfCnpj((String) request.get("cpfCnpj"));
        customer.setAddress((String) request.get("address"));
        customer.setCity((String) request.get("city"));
        customer.setState((String) request.get("state"));
        customer.setStatus("ACTIVE");

        if (request.get("planId") != null) {
            customer.setPlanId(((Number) request.get("planId")).longValue());
        }

        customer.setContractStart(LocalDateTime.now());
        customer = customerRepository.save(customer);

        log.info("[EXTERNAL_API] Cliente criado via API: {} - {}", customer.getId(), customer.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(customerToMap(customer));
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@RequestHeader(HEADER_API_KEY) String apiKey,
                                             @PathVariable Long id,
                                             @RequestBody Map<String, Object> request) {
        ResponseEntity<?> auth = authenticate(apiKey, "customers");
        if (auth != null) return auth;

        return customerRepository.findById(id)
                .map(customer -> {
                    if (request.containsKey("name")) customer.setName((String) request.get("name"));
                    if (request.containsKey("email")) customer.setEmail((String) request.get("email"));
                    if (request.containsKey("phone")) customer.setPhone((String) request.get("phone"));
                    if (request.containsKey("status")) customer.setStatus((String) request.get("status"));
                    if (request.containsKey("planId")) {
                        customer.setPlanId(((Number) request.get("planId")).longValue());
                    }
                    customer = customerRepository.save(customer);
                    return ResponseEntity.ok(customerToMap(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices")
    public ResponseEntity<?> listInvoices(@RequestHeader(HEADER_API_KEY) String apiKey,
                                           @RequestParam(required = false) Long customerId,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String referenceMonth,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int limit) {
        ResponseEntity<?> auth = authenticate(apiKey, "invoices");
        if (auth != null) return auth;

        List<Invoice> invoices = invoiceRepository.findAll();

        if (customerId != null) {
            invoices = invoices.stream().filter(i -> customerId.equals(i.getCustomerId())).toList();
        }
        if (status != null) {
            invoices = invoices.stream().filter(i -> status.equalsIgnoreCase(i.getStatus())).toList();
        }
        if (referenceMonth != null) {
            invoices = invoices.stream().filter(i -> referenceMonth.equals(i.getReferenceMonth())).toList();
        }

        invoices.sort(Comparator.comparing(Invoice::getCreatedAt).reversed());

        int start = page * limit;
        int end = Math.min(start + limit, invoices.size());
        List<Invoice> paged = start < invoices.size() ? invoices.subList(start, end) : List.of();

        List<Map<String, Object>> result = paged.stream().map(this::invoiceToMap).toList();

        return ResponseEntity.ok(Map.of(
                "data", result,
                "total", invoices.size(),
                "page", page,
                "limit", limit
        ));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoice(@RequestHeader(HEADER_API_KEY) String apiKey,
                                         @PathVariable Long id) {
        ResponseEntity<?> auth = authenticate(apiKey, "invoices");
        if (auth != null) return auth;

        return invoiceRepository.findById(id)
                .map(inv -> ResponseEntity.ok(invoiceToMap(inv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<?> payInvoice(@RequestHeader(HEADER_API_KEY) String apiKey,
                                          @PathVariable Long id) {
        ResponseEntity<?> auth = authenticate(apiKey, "collections");
        if (auth != null) return auth;

        Optional<Invoice> invOpt = invoiceRepository.findById(id);
        if (invOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Invoice invoice = invOpt.get();
        if ("PAID".equals(invoice.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fatura já paga"));
        }

        collectionService.processConciliation(id);

        log.info("[EXTERNAL_API] Fatura paga via API: {} - R$ {}", id, invoice.getTotalAmount());

        return ResponseEntity.ok(invoiceToMap(invoiceRepository.findById(id).get()));
    }

    @GetMapping("/plans")
    public ResponseEntity<?> listPlans(@RequestHeader(HEADER_API_KEY) String apiKey) {
        ResponseEntity<?> auth = authenticate(apiKey, "plans");
        if (auth != null) return auth;

        List<Plan> plans = planRepository.findAll();
        List<Map<String, Object>> result = plans.stream().map(this::planToMap).toList();

        return ResponseEntity.ok(Map.of("data", result));
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<?> getPlan(@RequestHeader(HEADER_API_KEY) String apiKey,
                                     @PathVariable Long id) {
        ResponseEntity<?> auth = authenticate(apiKey, "plans");
        if (auth != null) return auth;

        return planRepository.findById(id)
                .map(plan -> ResponseEntity.ok(planToMap(plan)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "NG Pro External API",
                "timestamp", LocalDateTime.now().toString(),
                "version", "1.0.0"
        ));
    }

    private Map<String, Object> customerToMap(Customer c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("name", c.getName());
        map.put("email", c.getEmail());
        map.put("phone", c.getPhone());
        map.put("cpfCnpj", c.getCpfCnpj());
        map.put("status", c.getStatus());
        map.put("planId", c.getPlanId());
        map.put("address", c.getAddress());
        map.put("city", c.getCity());
        map.put("state", c.getState());
        map.put("monthlyUsageMb", c.getMonthlyUsageMb());
        map.put("lastInvoiceAmount", c.getLastInvoiceAmount());
        map.put("contractStart", c.getContractStart() != null ? c.getContractStart().toString() : null);
        map.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> invoiceToMap(Invoice i) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", i.getId());
        map.put("customerId", i.getCustomerId());
        map.put("planId", i.getPlanId());
        map.put("status", i.getStatus());
        map.put("baseAmount", i.getBaseAmount());
        map.put("overageAmount", i.getOverageAmount());
        map.put("totalAmount", i.getTotalAmount());
        map.put("dueDate", i.getDueDate() != null ? i.getDueDate().toString() : null);
        map.put("paidAt", i.getPaidAt() != null ? i.getPaidAt().toString() : null);
        map.put("referenceMonth", i.getReferenceMonth());
        map.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> planToMap(Plan p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("downloadSpeed", p.getDownloadSpeed());
        map.put("uploadSpeed", p.getUploadSpeed());
        map.put("basePrice", p.getBasePrice());
        map.put("tier1LimitMb", p.getTier1LimitMb());
        map.put("tier2PricePerMb", p.getTier2PricePerMb());
        return map;
    }
}
