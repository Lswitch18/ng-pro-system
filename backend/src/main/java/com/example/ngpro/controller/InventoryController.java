package com.example.ngpro.controller;

import com.example.ngpro.model.Equipment;
import com.example.ngpro.model.Supplier;
import com.example.ngpro.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/equipment")
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createEquipment(equipment));
    }

    @GetMapping("/equipment")
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(inventoryService.getAllEquipment());
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return inventoryService.getEquipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/equipment/serial/{serial}")
    public ResponseEntity<Equipment> getEquipmentBySerial(@PathVariable String serial) {
        return inventoryService.getEquipmentBySerial(serial)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/equipment/status/{status}")
    public ResponseEntity<List<Equipment>> getEquipmentByStatus(@PathVariable Equipment.EquipmentStatus status) {
        return ResponseEntity.ok(inventoryService.getEquipmentByStatus(status));
    }

    @GetMapping("/equipment/type/{type}")
    public ResponseEntity<List<Equipment>> getEquipmentByType(@PathVariable Equipment.EquipmentType type) {
        return ResponseEntity.ok(inventoryService.getEquipmentByType(type));
    }

    @GetMapping("/equipment/customer/{customerId}")
    public ResponseEntity<List<Equipment>> getEquipmentByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(inventoryService.getEquipmentByCustomer(customerId));
    }

    @GetMapping("/equipment/available")
    public ResponseEntity<List<Equipment>> getAvailableEquipment() {
        return ResponseEntity.ok(inventoryService.getAvailableEquipment());
    }

    @PostMapping("/equipment/{id}/install")
    public ResponseEntity<?> installEquipment(@PathVariable Long id, 
            @RequestBody Map<String, Object> body) {
        try {
            Long customerId = ((Number) body.get("customerId")).longValue();
            Long serviceOrderId = body.get("serviceOrderId") != null ? ((Number) body.get("serviceOrderId")).longValue() : null;
            String technician = (String) body.getOrDefault("technician", "System");
            
            Equipment equipment = inventoryService.installEquipment(id, customerId, serviceOrderId, technician);
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/equipment/{id}/uninstall")
    public ResponseEntity<?> uninstallEquipment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            String technician = body.getOrDefault("technician", "System");
            Equipment equipment = inventoryService.uninstallEquipment(id, reason, technician);
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/equipment/{id}/transfer")
    public ResponseEntity<?> transferEquipment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long newCustomerId = ((Number) body.get("newCustomerId")).longValue();
            String technician = (String) body.getOrDefault("technician", "System");
            Equipment equipment = inventoryService.transferEquipment(id, newCustomerId, technician);
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/equipment/{id}/maintenance")
    public ResponseEntity<?> sendToMaintenance(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            String technician = body.getOrDefault("technician", "System");
            Equipment equipment = inventoryService.sendToMaintenance(id, reason, technician);
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/equipment/{id}/maintenance/return")
    public ResponseEntity<?> returnFromMaintenance(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String technician = body.getOrDefault("technician", "System");
            Equipment equipment = inventoryService.returnFromMaintenance(id, technician);
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/equipment/{id}/history")
    public ResponseEntity<?> getEquipmentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getEquipmentHistory(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getInventorySummary() {
        return ResponseEntity.ok(inventoryService.getInventorySummary());
    }

    @PostMapping("/suppliers")
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createSupplier(supplier));
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(inventoryService.getAllSuppliers());
    }

    @GetMapping("/equipment/types")
    public ResponseEntity<?> getEquipmentTypes() {
        return ResponseEntity.ok(Equipment.EquipmentType.values());
    }

    @GetMapping("/equipment/statuses")
    public ResponseEntity<?> getEquipmentStatuses() {
        return ResponseEntity.ok(Equipment.EquipmentStatus.values());
    }
}
