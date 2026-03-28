package com.example.ngpro.controller;

import com.example.ngpro.model.ServiceOrder;
import com.example.ngpro.model.Technician;
import com.example.ngpro.service.ServiceOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/service-orders")
@Slf4j
public class ServiceOrderController {

    @Autowired
    private ServiceOrderService serviceOrderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody ServiceOrder order) {
        try {
            ServiceOrder created = serviceOrderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[SERVICE_ORDER] Error creating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ServiceOrder>> getAllOrders() {
        return ResponseEntity.ok(serviceOrderService.getAllOrders());
    }

    @GetMapping("/open")
    public ResponseEntity<List<ServiceOrder>> getOpenOrders() {
        return ResponseEntity.ok(serviceOrderService.getOpenOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return serviceOrderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByNumber(@PathVariable String orderNumber) {
        return serviceOrderService.getOrderByNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ServiceOrder>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(serviceOrderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<ServiceOrder>> getOrdersByTechnician(@PathVariable Long technicianId) {
        return ResponseEntity.ok(serviceOrderService.getOrdersByTechnician(technicianId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServiceOrder>> getOrdersByStatus(@PathVariable ServiceOrder.ServiceOrderStatus status) {
        return ResponseEntity.ok(serviceOrderService.getOrdersByStatus(status));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<ServiceOrder>> getScheduledOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(serviceOrderService.getScheduledOrders(start, end));
    }

    @PostMapping("/{id}/assign/{technicianId}")
    public ResponseEntity<?> assignTechnician(@PathVariable Long id, @PathVariable Long technicianId) {
        try {
            ServiceOrder updated = serviceOrderService.assignTechnician(id, technicianId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[SERVICE_ORDER] Error assigning technician: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            ServiceOrder.ServiceOrderStatus newStatus = ServiceOrder.ServiceOrderStatus.valueOf(body.get("status"));
            ServiceOrder updated = serviceOrderService.updateStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[SERVICE_ORDER] Error updating status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String resolution = body.get("resolution");
            ServiceOrder completed = serviceOrderService.completeOrder(id, resolution);
            return ResponseEntity.ok(completed);
        } catch (Exception e) {
            log.error("[SERVICE_ORDER] Error completing order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            ServiceOrder cancelled = serviceOrderService.cancelOrder(id, reason);
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            log.error("[SERVICE_ORDER] Error cancelling order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String message = (String) body.get("message");
            String userName = (String) body.getOrDefault("userName", "System");
            String userType = (String) body.getOrDefault("userType", "OPERATOR");
            boolean isInternal = (Boolean) body.getOrDefault("isInternal", false);
            
            serviceOrderService.addComment(id, message, userName, userType, isInternal);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("[SERVICE_ORDER] Error adding comment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/technicians")
    public ResponseEntity<List<Technician>> getAllTechnicians() {
        return ResponseEntity.ok(serviceOrderService.getAllTechnicians());
    }

    @GetMapping("/technicians/available")
    public ResponseEntity<List<Technician>> getAvailableTechnicians() {
        return ResponseEntity.ok(serviceOrderService.getAvailableTechnicians());
    }

    @PostMapping("/technicians")
    public ResponseEntity<Technician> createTechnician(@RequestBody Technician technician) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.createTechnician(technician));
    }

    @PostMapping("/auto-assign/{orderId}")
    public ResponseEntity<?> autoAssign(@PathVariable Long orderId) {
        try {
            ServiceOrder order = serviceOrderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            Technician bestTech = serviceOrderService.findBestTechnician(
                    order.getCity(), 
                    order.getType()
            );
            
            if (bestTech == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Nenhum técnico disponível encontrado"));
            }
            
            ServiceOrder updated = serviceOrderService.assignTechnician(orderId, bestTech.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[SERVICE_ORDER] Error auto-assigning: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
