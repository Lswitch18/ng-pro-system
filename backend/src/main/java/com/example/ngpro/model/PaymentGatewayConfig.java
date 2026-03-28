package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_gateway_config")
@Data
public class PaymentGatewayConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gateway_name")
    private String gatewayName; // MERCADOPAGO, PAGARME, STRIPE

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_production")
    private boolean isProduction;

    @Column(name = "client_id")
    private String clientId; // Client ID do gateway

    @Column(name = "client_secret")
    private String clientSecret; // Client Secret (criptografar em produção!)

    @Column(name = "access_token")
    private String accessToken; // Access Token

    @Column(name = "public_key")
    private String publicKey; // Public Key para frontend

    @Column(name = "webhook_url")
    private String webhookUrl; // URL do webhook

    @Column(name = "webhook_secret")
    private String webhookSecret; // Secret para validar webhook

    @Column(name = "pix_key_type")
    private String pixKeyType; // CPF, CNPJ, EMAIL, PHONE, RANDOM

    @Column(name = "pix_key")
    private String pixKey; // Chave PIX

    @Column(name = "pix_beneficiary_name")
    private String pixBeneficiaryName; // Nome do beneficiário

    @Column(name = "pix_beneficiary_cnpj")
    private String pixBeneficiaryCnpj; // CNPJ do beneficiário

    @Column(name = "bank_code")
    private String bankCode; // Código do banco

    @Column(name = "bank_agency")
    private String bankAgency; // Agência

    @Column(name = "bank_account")
    private String bankAccount; // Conta

    @Column(name = "bank_account_type")
    private String bankAccountType; // CHECKING, SAVINGS

    @Column(name = "expiration_hours")
    private Integer expirationHours; // Horas até expirar (PIX/Boleto)

    @Column(name = "tax_id")
    private String taxId; // CNPJ da empresa (para NFC-e)

    @Column(name = "merchant_id")
    private String merchantId; // ID do estabelecimento

    @Column(name = "mcc_code")
    private String mccCode; // Merchant Category Code

    @Column(name = "callback_url")
    private String callbackUrl; // URL de retorno após pagamento

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
