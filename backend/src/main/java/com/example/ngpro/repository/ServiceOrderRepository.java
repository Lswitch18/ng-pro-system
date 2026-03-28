package com.example.ngpro.repository;

import com.example.ngpro.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    List<ServiceOrder> findByCustomerId(Long customerId);
    List<ServiceOrder> findByTechnicianId(Long technicianId);
    List<ServiceOrder> findByStatus(ServiceOrder.ServiceOrderStatus status);
    List<ServiceOrder> findByType(ServiceOrder.ServiceOrderType type);
    List<ServiceOrder> findByStatusIn(List<ServiceOrder.ServiceOrderStatus> statuses);
    Optional<ServiceOrder> findByOrderNumber(String orderNumber);
    List<ServiceOrder> findByScheduledDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
