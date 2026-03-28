package com.example.ngpro.repository;

import com.example.ngpro.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findBySerialNumber(String serialNumber);
    Optional<Equipment> findByMacAddress(String macAddress);
    List<Equipment> findByStatus(Equipment.EquipmentStatus status);
    List<Equipment> findByEquipmentType(Equipment.EquipmentType type);
    List<Equipment> findByCustomerId(Long customerId);
    List<Equipment> findBySupplierId(Long supplierId);
    List<Equipment> findByStatusIn(List<Equipment.EquipmentStatus> statuses);
    long countByStatus(Equipment.EquipmentStatus status);
    long countByEquipmentType(Equipment.EquipmentType type);
}
