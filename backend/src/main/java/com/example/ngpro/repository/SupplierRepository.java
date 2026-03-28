package com.example.ngpro.repository;

import com.example.ngpro.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByCnpj(String cnpj);
    List<Supplier> findByIsActive(boolean isActive);
    List<Supplier> findByCategory(String category);
}
