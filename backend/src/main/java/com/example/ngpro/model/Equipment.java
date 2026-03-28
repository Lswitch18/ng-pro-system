package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Data
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(name = "mac_address", unique = true)
    private String macAddress;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model")
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type")
    private EquipmentType equipmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EquipmentStatus status;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "warranty_end_date")
    private LocalDateTime warrantyEndDate;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "bar_code")
    private String barCode;

    @Column(name = "purchase_value")
    private Double purchaseValue;

    @Column(name = "current_value")
    private Double currentValue;

    @Column(name = "color")
    private String color;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = EquipmentStatus.STOCK;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum EquipmentType {
        ONT,
        ROUTER,
        SWITCH,
        OLT,
        FIBER_CABLE,
        UTP_CABLE,
        CONNECTOR,
        SPLITTER,
        ANTENNA,
        UPS,
        COMPUTER,
        PHONE,
        CAMERA,
        ACCESS_POINT,
        OTHER
    }

    public enum EquipmentStatus {
        STOCK,
        RESERVED,
        INSTALLED,
        MAINTENANCE,
        DEFECTIVE,
        RETIRED,
        LOST,
        STOLEN
    }
}
