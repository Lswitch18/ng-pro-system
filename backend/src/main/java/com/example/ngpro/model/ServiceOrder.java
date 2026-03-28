package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_orders")
@Data
public class ServiceOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "technician_id")
    private Long technicianId;

    @Enumerated(EnumType.STRING)
    private ServiceOrderType type;

    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus status;

    @Enumerated(EnumType.STRING)
    private ServiceOrderPriority priority;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "customer_signature", columnDefinition = "TEXT")
    private String customerSignature;

    @Column(name = "customer_rating")
    private Integer customerRating;

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = ServiceOrderStatus.OPEN;
        if (this.priority == null) this.priority = ServiceOrderPriority.MEDIUM;
        if (this.orderNumber == null) {
            this.orderNumber = "OS-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ServiceOrderType {
        INSTALLATION,
        MAINTENANCE,
        SUPPORT,
        UNINSTALL,
        CHANGE_PLAN,
        NETWORK_REPAIR,
        EQUIPMENT_CHANGE,
        INSPECTION,
        OTHER
    }

    public enum ServiceOrderStatus {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        PENDING_PARTS,
        COMPLETED,
        CANCELLED,
        REOPENED
    }

    public enum ServiceOrderPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
