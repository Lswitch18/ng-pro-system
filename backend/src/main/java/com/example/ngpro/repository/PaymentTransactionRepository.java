package com.example.ngpro.repository;

import com.example.ngpro.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByInvoiceId(Long invoiceId);
    List<PaymentTransaction> findByCustomerId(Long customerId);
    Optional<PaymentTransaction> findByExternalId(String externalId);
    Optional<PaymentTransaction> findByExternalReference(String externalReference);
    List<PaymentTransaction> findByStatus(String status);
    List<PaymentTransaction> findByStatusAndExpiresAtBefore(String status, java.time.LocalDateTime dateTime);
}
