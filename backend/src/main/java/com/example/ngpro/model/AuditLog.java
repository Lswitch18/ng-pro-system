package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private AuditAction action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        PASSWORD_CHANGE,
        ACCESS_DENIED,
        PAYMENT_RECEIVED,
        PAYMENT_FAILED,
        INVOICE_GENERATED,
        SERVICE_ACTIVATED,
        SERVICE_SUSPENDED,
        SERVICE_BLOCKED,
        EQUIPMENT_INSTALLED,
        EQUIPMENT_REMOVED,
        ORDER_CREATED,
        ORDER_COMPLETED,
        ORDER_CANCELLED,
        CONFIG_CHANGED,
        API_ACCESS,
        EXPORT_DATA,
        IMPORT_DATA
    }
}
