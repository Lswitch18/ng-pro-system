package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "technicians")
@Data
public class Technician {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "cpf", unique = true)
    private String cpf;

    @Column(name = "region")
    private String region;

    @ElementCollection
    @CollectionTable(name = "technician_specialties", joinColumns = @JoinColumn(name = "technician_id"))
    @Column(name = "specialty")
    private List<String> specialties;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TechnicianStatus status;

    @Column(name = "current_orders_count")
    private Integer currentOrdersCount;

    @Column(name = "max_orders")
    private Integer maxOrders;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = TechnicianStatus.AVAILABLE;
        if (this.currentOrdersCount == null) this.currentOrdersCount = 0;
        if (this.maxOrders == null) this.maxOrders = 10;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TechnicianStatus {
        AVAILABLE,
        BUSY,
        OFF,
        ON_VACATION,
        INACTIVE
    }
}
