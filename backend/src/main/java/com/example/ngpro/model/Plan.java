package com.example.ngpro.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "plans")
@Data
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double basePrice;
    private String speed;       // ex: "500 Mbps"
    private long dataCapMb;     // 0 = ilimitado
    private boolean familyPlan;

    // Tiered pricing thresholds
    @Column(name = "tier1_limit_mb")
    private long tier1LimitMb;  // até este limite cobra basePrice
    @Column(name = "tier2_price_per_mb")
    private double tier2PricePerMb; // excedente
}
