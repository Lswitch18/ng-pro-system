package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_movements")
@Data
public class EquipmentMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type")
    private MovementType movementType;

    @Column(name = "from_location")
    private String fromLocation;

    @Column(name = "to_location")
    private String toLocation;

    @Column(name = "from_customer_id")
    private Long fromCustomerId;

    @Column(name = "to_customer_id")
    private Long toCustomerId;

    @Column(name = "service_order_id")
    private Long serviceOrderId;

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum MovementType {
        PURCHASE,
        STOCK_IN,
        INSTALLATION,
        UNINSTALLATION,
        TRANSFER,
        MAINTENANCE_IN,
        MAINTENANCE_OUT,
        RETURN,
        RESALE,
        RETIRED,
        LOST,
        STOLEN,
        DAMAGED
    }
}
