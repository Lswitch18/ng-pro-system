package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "gateway_type")
    private String gatewayType; // MERCADOPAGO, PAGARME, PIX_DIRECT

    @Column(name = "payment_method")
    private String paymentMethod; // PIX, BOLETO, CREDIT_CARD, DEBIT_CARD

    @Column(name = "external_id")
    private String externalId; // ID do pagamento no gateway

    @Column(name = "external_reference")
    private String externalReference; // Referência no gateway

    @Column(name = "qr_code")
    private String qrCode; // QR Code PIX base64

    @Column(name = "qr_code_text")
    private String qrCodeText; // Texto do QR Code PIX Copia e Cola

    @Column(name = "boleto_url")
    private String boletoUrl; // URL do boleto

    @Column(name = "boleto_barcode")
    private String boletoBarcode; // Código de barras

    @Column(name = "amount")
    private double amount;

    @Column(name = "status")
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED, REFUNDED

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "payment_details", columnDefinition = "TEXT")
    private String paymentDetails; // JSON com detalhes adicionais

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
