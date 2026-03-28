package com.example.ngpro.controller;

import com.example.ngpro.model.Invoice;
import com.example.ngpro.model.PaymentTransaction;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.service.MercadoPagoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @PostMapping("/pix/{invoiceId}")
    public ResponseEntity<?> createPixPayment(@PathVariable Long invoiceId) {
        try {
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Invoice invoice = invoiceOpt.get();
            if ("PAID".equals(invoice.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Fatura já está paga",
                        "invoiceId", invoiceId,
                        "status", invoice.getStatus()
                ));
            }

            PaymentTransaction transaction = mercadoPagoService.createPixPayment(invoiceId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentId", transaction.getId(),
                    "invoiceId", invoiceId,
                    "amount", transaction.getAmount(),
                    "status", transaction.getStatus(),
                    "qrCode", transaction.getQrCodeText(),
                    "qrCodeBase64", transaction.getQrCode() != null ? transaction.getQrCode() : "",
                    "expiresAt", transaction.getExpiresAt() != null ? transaction.getExpiresAt().toString() : ""
            ));
        } catch (Exception e) {
            log.error("[PAYMENT] Error creating PIX payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/boleto/{invoiceId}")
    public ResponseEntity<?> createBoletoPayment(@PathVariable Long invoiceId) {
        try {
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Invoice invoice = invoiceOpt.get();
            if ("PAID".equals(invoice.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Fatura já está paga",
                        "invoiceId", invoiceId,
                        "status", invoice.getStatus()
                ));
            }

            PaymentTransaction transaction = mercadoPagoService.createBoletoPayment(invoiceId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentId", transaction.getId(),
                    "invoiceId", invoiceId,
                    "amount", transaction.getAmount(),
                    "status", transaction.getStatus(),
                    "boletoUrl", transaction.getBoletoUrl() != null ? transaction.getBoletoUrl() : "",
                    "boletoBarcode", transaction.getBoletoBarcode() != null ? transaction.getBoletoBarcode() : "",
                    "expiresAt", transaction.getExpiresAt() != null ? transaction.getExpiresAt().toString() : ""
            ));
        } catch (Exception e) {
            log.error("[PAYMENT] Error creating Boleto payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        List<PaymentTransaction> transactions = mercadoPagoService.getTransactionsByInvoice(invoiceId);
        return ResponseEntity.ok(Map.of(
                "invoiceId", invoiceId,
                "payments", transactions
        ));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getPaymentsByCustomer(@PathVariable Long customerId) {
        List<PaymentTransaction> transactions = mercadoPagoService.getTransactionsByCustomer(customerId);
        return ResponseEntity.ok(Map.of(
                "customerId", customerId,
                "payments", transactions
        ));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "message", "Payment details endpoint - implement if needed"
        ));
    }

    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<?> handleMercadoPagoWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("[WEBHOOK] Received MercadoPago webhook: {}", payload);
            mercadoPagoService.processWebhook(payload);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("[WEBHOOK] Error processing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/methods")
    public ResponseEntity<?> getAvailablePaymentMethods() {
        return ResponseEntity.ok(Map.of(
                "methods", List.of(
                        Map.of("id", "PIX", "name", "PIX", "description", "Pagamento instantâneo"),
                        Map.of("id", "BOLETO", "name", "Boleto Bancário", "description", "Pagamento por boleto"),
                        Map.of("id", "CREDIT_CARD", "name", "Cartão de Crédito", "description", "Parcelamento em até 12x"),
                        Map.of("id", "DEBIT_CARD", "name", "Cartão de Débito", "description", "Débito online")
                )
        ));
    }

    @PostMapping("/{paymentId}/check-status")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "message", "Check status endpoint - implement if needed"
        ));
    }
}
