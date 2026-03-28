package com.example.ngpro.repository;

import com.example.ngpro.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCustomerId(Long customerId);
    List<Invoice> findByStatus(String status);
    Page<Invoice> findByStatus(String status, Pageable pageable);
    List<Invoice> findByReferenceMonth(String referenceMonth);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PAID'")
    Double sumPaidRevenue();

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PENDING'")
    Double sumPendingRevenue();
}
