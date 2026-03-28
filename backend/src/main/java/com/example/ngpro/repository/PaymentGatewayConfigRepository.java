package com.example.ngpro.repository;

import com.example.ngpro.model.PaymentGatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentGatewayConfigRepository extends JpaRepository<PaymentGatewayConfig, Long> {
    Optional<PaymentGatewayConfig> findByGatewayNameAndIsActive(String gatewayName, boolean isActive);
    Optional<PaymentGatewayConfig> findTopByIsActiveOrderByIdDesc(boolean isActive);
}
