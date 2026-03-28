package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "plan_id")
    private Long planId;

    private String status; // PENDING, PAID, OVERDUE, CANCELLED

    @Column(name = "base_amount")
    private double baseAmount;

    @Column(name = "overage_amount")
    private double overageAmount;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "reference_month")
    private String referenceMonth; // ex: "2026-03"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "billing_process_id")
    private String billingProcessId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
        this.totalAmount = this.baseAmount + this.overageAmount;
    }
}
