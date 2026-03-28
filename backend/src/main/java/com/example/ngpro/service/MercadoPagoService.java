package com.example.ngpro.service;

import com.example.ngpro.model.Customer;
import com.example.ngpro.model.Invoice;
import com.example.ngpro.model.PaymentGatewayConfig;
import com.example.ngpro.model.PaymentTransaction;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.repository.PaymentGatewayConfigRepository;
import com.example.ngpro.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class MercadoPagoService {

    @Autowired
    private PaymentTransactionRepository paymentRepo;

    @Autowired
    private PaymentGatewayConfigRepository gatewayConfigRepo;

    @Autowired
    private InvoiceRepository invoiceRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private CollectionService collectionService;

    @Value("${mercadopago.webhook.url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MERCADOPAGO_API = "https://api.mercadopago.com";
    private static final String MERCADOPAGO_SANDBOX = "https://api.mercadopago.com";

    public PaymentTransaction createPixPayment(Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        PaymentGatewayConfig config = getActiveGateway();
        String accessToken = config.getAccessToken();
        String baseUrl = config.isProduction() ? MERCADOPAGO_API : MERCADOPAGO_SANDBOX;

        Customer customer = customerRepo.findById(invoice.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transaction_amount", invoice.getTotalAmount());
        requestBody.put("description", "Fatura " + invoice.getReferenceMonth() + " - " + customer.getName());
        requestBody.put("payment_method_id", "pix");
        requestBody.put("payer", Map.of(
                "email", customer.getEmail() != null ? customer.getEmail() : "email@cliente.com",
                "identification", Map.of(
                        "type", customer.getCpfCnpj() != null && customer.getCpfCnpj().length() > 14 ? "CNPJ" : "CPF",
                        "number", customer.getCpfCnpj() != null ? customer.getCpfCnpj().replaceAll("[^0-9]", "") : "00000000000"
                )
        ));
        requestBody.put("external_reference", "INV_" + invoice.getId() + "_" + System.currentTimeMillis());
        requestBody.put("notification_url", webhookUrl);

        if (config.getExpirationHours() != null && config.getExpirationHours() > 0) {
            requestBody.put("date_of_expiration", LocalDateTime.now()
                    .plusHours(config.getExpirationHours())
                    .toString().replace("T", "T"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/v1/payments";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                JsonNode json = objectMapper.readTree(response.getBody());

                PaymentTransaction transaction = new PaymentTransaction();
                transaction.setInvoiceId(invoiceId);
                transaction.setCustomerId(invoice.getCustomerId());
                transaction.setGatewayType("MERCADOPAGO");
                transaction.setPaymentMethod("PIX");
                transaction.setExternalId(json.has("id") ? json.get("id").asText() : null);
                transaction.setExternalReference(json.has("external_reference") ? json.get("external_reference").asText() : null);
                transaction.setAmount(invoice.getTotalAmount());
                transaction.setStatus(json.has("status") ? json.get("status").asText() : "pending");

                if (json.has("point_of_interaction")) {
                    JsonNode poi = json.get("point_of_interaction");
                    if (poi.has("transaction_data")) {
                        JsonNode td = poi.get("transaction_data");
                        if (td.has("qr_code")) {
                            transaction.setQrCodeText(td.get("qr_code").asText());
                        }
                        if (td.has("qr_code_base64")) {
                            transaction.setQrCode(td.get("qr_code_base64").asText());
                        }
                    }
                }

                if (config.getExpirationHours() != null) {
                    transaction.setExpiresAt(LocalDateTime.now().plusHours(config.getExpirationHours()));
                }

                transaction.setPaymentDetails(response.getBody());
                transaction = paymentRepo.save(transaction);

                log.info("[MERCADOPAGO] PIX Payment created: {} for Invoice: {}", transaction.getExternalId(), invoiceId);
                return transaction;
            } else {
                throw new RuntimeException("Failed to create PIX payment: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("[MERCADOPAGO] Error creating PIX payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PIX payment: " + e.getMessage());
        }
    }

    public PaymentTransaction createBoletoPayment(Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        PaymentGatewayConfig config = getActiveGateway();
        String accessToken = config.getAccessToken();
        String baseUrl = config.isProduction() ? MERCADOPAGO_API : MERCADOPAGO_SANDBOX;

        Customer customer = customerRepo.findById(invoice.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transaction_amount", invoice.getTotalAmount());
        requestBody.put("description", "Fatura " + invoice.getReferenceMonth() + " - " + customer.getName());
        requestBody.put("payment_method_id", "bolitopayment");
        requestBody.put("payer", Map.of(
                "email", customer.getEmail() != null ? customer.getEmail() : "email@cliente.com",
                "first_name", customer.getName() != null ? customer.getName().split(" ")[0] : "Cliente",
                "last_name", customer.getName() != null && customer.getName().contains(" ") 
                        ? customer.getName().substring(customer.getName().indexOf(" ") + 1) : "",
                "identification", Map.of(
                        "type", customer.getCpfCnpj() != null && customer.getCpfCnpj().length() > 14 ? "CNPJ" : "CPF",
                        "number", customer.getCpfCnpj() != null ? customer.getCpfCnpj().replaceAll("[^0-9]", "") : "00000000000"
                ),
                "address", Map.of(
                        "zip_code", customer.getAddress() != null ? "80000000" : "80000000",
                        "street_name", customer.getAddress() != null ? customer.getAddress() : "Rua",
                        "street_number", "0",
                        "city", customer.getCity() != null ? customer.getCity() : "Cidade",
                        "state", customer.getState() != null ? customer.getState() : "UF"
                )
        ));
        requestBody.put("external_reference", "INV_" + invoice.getId() + "_" + System.currentTimeMillis());
        requestBody.put("notification_url", webhookUrl);

        if (config.getExpirationHours() != null && config.getExpirationHours() > 0) {
            requestBody.put("date_of_expiration", LocalDateTime.now()
                    .plusDays(3)
                    .toString().replace("T", "T"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/v1/payments";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                JsonNode json = objectMapper.readTree(response.getBody());

                PaymentTransaction transaction = new PaymentTransaction();
                transaction.setInvoiceId(invoiceId);
                transaction.setCustomerId(invoice.getCustomerId());
                transaction.setGatewayType("MERCADOPAGO");
                transaction.setPaymentMethod("BOLETO");
                transaction.setExternalId(json.has("id") ? json.get("id").asText() : null);
                transaction.setExternalReference(json.has("external_reference") ? json.get("external_reference").asText() : null);
                transaction.setAmount(invoice.getTotalAmount());
                transaction.setStatus(json.has("status") ? json.get("status").asText() : "pending");

                if (json.has("barcode")) {
                    transaction.setBoletoBarcode(json.get("barcode").asText());
                }
                if (json.has("transaction_details")) {
                    JsonNode td = json.get("transaction_details");
                    if (td.has("external_resource_url")) {
                        transaction.setBoletoUrl(td.get("external_resource_url").asText());
                    }
                }

                if (config.getExpirationHours() != null) {
                    transaction.setExpiresAt(LocalDateTime.now().plusDays(3));
                }

                transaction.setPaymentDetails(response.getBody());
                transaction = paymentRepo.save(transaction);

                log.info("[MERCADOPAGO] Boleto Payment created: {} for Invoice: {}", transaction.getExternalId(), invoiceId);
                return transaction;
            } else {
                throw new RuntimeException("Failed to create Boleto payment: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("[MERCADOPAGO] Error creating Boleto payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Boleto payment: " + e.getMessage());
        }
    }

    public PaymentTransaction getPaymentStatus(String externalId) {
        PaymentGatewayConfig config = getActiveGateway();
        String accessToken = config.getAccessToken();
        String baseUrl = config.isProduction() ? MERCADOPAGO_API : MERCADOPAGO_SANDBOX;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            String url = baseUrl + "/v1/payments/" + externalId;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode json = objectMapper.readTree(response.getBody());
                
                return paymentRepo.findByExternalId(externalId).map(t -> {
                    t.setStatus(json.has("status") ? json.get("status").asText() : t.getStatus());
                    if ("approved".equalsIgnoreCase(json.get("status").asText())) {
                        t.setPaidAt(LocalDateTime.now());
                    }
                    t.setPaymentDetails(response.getBody());
                    return paymentRepo.save(t);
                }).orElse(null);
            }
        } catch (Exception e) {
            log.error("[MERCADOPAGO] Error getting payment status: {}", e.getMessage());
        }
        return null;
    }

    public void processWebhook(Map<String, Object> payload) {
        try {
            String topic = (String) payload.get("topic");
            String action = (String) payload.get("action");

            log.info("[MERCADOPAGO] Webhook received: topic={}, action={}", topic, action);

            if ("payment".equals(topic)) {
                String paymentId = String.valueOf(payload.get("resource"));
                if (paymentId != null && !paymentId.isEmpty()) {
                    PaymentTransaction transaction = getPaymentStatus(paymentId);
                    if (transaction != null && "approved".equalsIgnoreCase(transaction.getStatus())) {
                        processSuccessfulPayment(transaction.getInvoiceId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[MERCADOPAGO] Error processing webhook: {}", e.getMessage(), e);
        }
    }

    public void processSuccessfulPayment(Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId).orElse(null);
        if (invoice == null) {
            log.error("[PAYMENT] Invoice not found: {}", invoiceId);
            return;
        }

        if ("PAID".equals(invoice.getStatus())) {
            log.info("[PAYMENT] Invoice already paid: {}", invoiceId);
            return;
        }

        log.info("[PAYMENT] Processing successful payment for Invoice: {}", invoiceId);
        
        collectionService.processConciliation(invoiceId);
        
        if (invoice.getCustomerId() != null) {
            provisioningService.activateService(invoice.getCustomerId());
        }
        
        log.info("[PAYMENT] Payment processed successfully for Invoice: {}", invoiceId);
    }

    private PaymentGatewayConfig getActiveGateway() {
        return gatewayConfigRepo.findTopByIsActiveOrderByIdDesc(true)
                .orElseGet(() -> {
                    PaymentGatewayConfig config = new PaymentGatewayConfig();
                    config.setGatewayName("MERCADOPAGO");
                    config.setActive(true);
                    config.setProduction(false);
                    config.setAccessToken(System.getenv().getOrDefault("MERCADOPAGO_ACCESS_TOKEN", "TEST_ACCESS_TOKEN"));
                    config.setWebhookUrl(webhookUrl);
                    config.setExpirationHours(24);
                    return gatewayConfigRepo.save(config);
                });
    }

    public List<PaymentTransaction> getTransactionsByInvoice(Long invoiceId) {
        return paymentRepo.findByInvoiceId(invoiceId);
    }

    public List<PaymentTransaction> getTransactionsByCustomer(Long customerId) {
        return paymentRepo.findByCustomerId(customerId);
    }
}
