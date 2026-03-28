package com.example.ngpro.service;

import com.example.ngpro.model.Equipment;
import com.example.ngpro.model.EquipmentMovement;
import com.example.ngpro.model.Supplier;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.EquipmentRepository;
import com.example.ngpro.repository.SupplierRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Equipment createEquipment(Equipment equipment) {
        equipment = equipmentRepository.save(equipment);
        log.info("[INVENTORY] Equipment created: {} - {}", equipment.getSerialNumber(), equipment.getEquipmentType());
        return equipment;
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    public Optional<Equipment> getEquipmentBySerial(String serial) {
        return equipmentRepository.findBySerialNumber(serial);
    }

    public List<Equipment> getEquipmentByStatus(Equipment.EquipmentStatus status) {
        return equipmentRepository.findByStatus(status);
    }

    public List<Equipment> getEquipmentByType(Equipment.EquipmentType type) {
        return equipmentRepository.findByEquipmentType(type);
    }

    public List<Equipment> getEquipmentByCustomer(Long customerId) {
        return equipmentRepository.findByCustomerId(customerId);
    }

    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.findByStatus(Equipment.EquipmentStatus.STOCK);
    }

    @Transactional
    public Equipment installEquipment(Long equipmentId, Long customerId, Long serviceOrderId, String technician) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (equipment.getStatus() != Equipment.EquipmentStatus.STOCK && equipment.getStatus() != Equipment.EquipmentStatus.RESERVED) {
            throw new RuntimeException("Equipment is not available for installation");
        }

        Equipment oldEquipment = equipment;
        equipment.setCustomerId(customerId);
        equipment.setStatus(Equipment.EquipmentStatus.INSTALLED);
        equipment.setInstallationDate(LocalDateTime.now());
        equipment = equipmentRepository.save(equipment);

        recordMovement(equipmentId, EquipmentMovement.MovementType.INSTALLATION,
                "STOCK", "CUSTOMER:" + customerId, null, customerId, serviceOrderId, null,
                "Installed for customer", technician);

        log.info("[INVENTORY] Equipment {} installed for customer {}", equipment.getSerialNumber(), customerId);

        return equipment;
    }

    @Transactional
    public Equipment uninstallEquipment(Long equipmentId, String reason, String technician) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        Long oldCustomerId = equipment.getCustomerId();
        
        equipment.setCustomerId(null);
        equipment.setStatus(Equipment.EquipmentStatus.STOCK);
        equipment.setInstallationDate(null);
        equipment = equipmentRepository.save(equipment);

        recordMovement(equipmentId, EquipmentMovement.MovementType.UNINSTALLATION,
                "CUSTOMER:" + oldCustomerId, "STOCK", oldCustomerId, null, null, null,
                reason, technician);

        log.info("[INVENTORY] Equipment {} uninstalled", equipment.getSerialNumber());

        return equipment;
    }

    @Transactional
    public Equipment transferEquipment(Long equipmentId, Long newCustomerId, String technician) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        Long oldCustomerId = equipment.getCustomerId();
        equipment.setCustomerId(newCustomerId);
        equipment = equipmentRepository.save(equipment);

        recordMovement(equipmentId, EquipmentMovement.MovementType.TRANSFER,
                "CUSTOMER:" + oldCustomerId, "CUSTOMER:" + newCustomerId,
                oldCustomerId, newCustomerId, null, null,
                "Equipment transfer", technician);

        log.info("[INVENTORY] Equipment {} transferred from customer {} to {}", 
                equipment.getSerialNumber(), oldCustomerId, newCustomerId);

        return equipment;
    }

    @Transactional
    public Equipment sendToMaintenance(Long equipmentId, String reason, String technician) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        equipment.setStatus(Equipment.EquipmentStatus.MAINTENANCE);
        equipment = equipmentRepository.save(equipment);

        recordMovement(equipmentId, EquipmentMovement.MovementType.MAINTENANCE_IN,
                equipment.getCustomerId() != null ? "CUSTOMER:" + equipment.getCustomerId() : "STOCK",
                "MAINTENANCE", equipment.getCustomerId(), null, null, null,
                reason, technician);

        log.info("[INVENTORY] Equipment {} sent to maintenance", equipment.getSerialNumber());

        return equipment;
    }

    @Transactional
    public Equipment returnFromMaintenance(Long equipmentId, String technician) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        equipment.setStatus(Equipment.EquipmentStatus.STOCK);
        equipment = equipmentRepository.save(equipment);

        recordMovement(equipmentId, EquipmentMovement.MovementType.MAINTENANCE_OUT,
                "MAINTENANCE", "STOCK", null, null, null, null,
                "Returned from maintenance", technician);

        log.info("[INVENTORY] Equipment {} returned from maintenance", equipment.getSerialNumber());

        return equipment;
    }

    private void recordMovement(Long equipmentId, EquipmentMovement.MovementType type,
                               String from, String to, Long fromCustomer, Long toCustomer,
                               Long serviceOrder, Long technician, String reason, String performedBy) {
        EquipmentMovement movement = new EquipmentMovement();
        movement.setEquipmentId(equipmentId);
        movement.setMovementType(type);
        movement.setFromLocation(from);
        movement.setToLocation(to);
        movement.setFromCustomerId(fromCustomer);
        movement.setToCustomerId(toCustomer);
        movement.setServiceOrderId(serviceOrder);
        movement.setTechnicianId(technician);
        movement.setReason(reason);
        movement.setPerformedBy(performedBy);
        
        log.info("[INVENTORY] Movement recorded: {} - {} -> {}", type, from, to);
    }

    public List<EquipmentMovement> getEquipmentHistory(Long equipmentId) {
        log.info("[INVENTORY] Getting history for equipment {}", equipmentId);
        return List.of();
    }

    public Map<String, Long> getInventorySummary() {
        return Map.of(
                "total", equipmentRepository.count(),
                "inStock", equipmentRepository.countByStatus(Equipment.EquipmentStatus.STOCK),
                "installed", equipmentRepository.countByStatus(Equipment.EquipmentStatus.INSTALLED),
                "maintenance", equipmentRepository.countByStatus(Equipment.EquipmentStatus.MAINTENANCE),
                "defective", equipmentRepository.countByStatus(Equipment.EquipmentStatus.DEFECTIVE)
        );
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
}
