package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Data
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "api_key", unique = true)
    private String apiKey;

    @Column(name = "client_type")
    private String clientType; // PRESTASHOP, ERP, MOBILE, etc

    private String permissions; // COMMA SEPARATED: customers,invoices,plans,collections

    private String status; // ACTIVE, INACTIVE, REVOKED

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "rate_limit_per_hour")
    private Integer rateLimitPerHour;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.rateLimitPerHour == null) this.rateLimitPerHour = 100;
    }
}
