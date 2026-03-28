package com.example.ngpro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @Email(message = "Email inválido")
    private String email;

    @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 dígitos")
    private String phone;

    private String cpfCnpj;

    @Pattern(regexp = "^(ACTIVE|SUSPENDED|BLOCKED)$", message = "Status deve ser ACTIVE, SUSPENDED ou BLOCKED")
    private String status;

    @Column(name = "plan_id")
    private Long planId;

    private String address;
    private String city;
    private String state;

    @Column(name = "contract_start")
    private LocalDateTime contractStart;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "monthly_usage_mb")
    private double monthlyUsageMb;

    @Column(name = "last_invoice_amount")
    private double lastInvoiceAmount;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
    }
}
