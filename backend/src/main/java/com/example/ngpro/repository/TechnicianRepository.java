package com.example.ngpro.repository;

import com.example.ngpro.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    List<Technician> findByStatus(Technician.TechnicianStatus status);
    List<Technician> findByRegion(String region);
    Optional<Technician> findByUserId(Long userId);
    Optional<Technician> findByCpf(String cpf);
    List<Technician> findByCurrentOrdersCountLessThan(Integer maxOrders);
}
