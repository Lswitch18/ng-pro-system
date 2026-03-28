package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_order_comments")
@Data
public class ServiceOrderComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_type")
    private String userType; // ADMIN, OPERATOR, TECHNICIAN, CUSTOMER

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_internal")
    private boolean isInternal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
