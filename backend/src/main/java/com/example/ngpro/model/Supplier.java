package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "cnpj", unique = true)
    private String cnpj;

    @Column(name = "ie")
    private String ie;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "website")
    private String website;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "category")
    private String category;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "credit_limit")
    private Double creditLimit;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (!this.isActive) this.isActive = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
