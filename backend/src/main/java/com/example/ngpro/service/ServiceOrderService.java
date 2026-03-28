package com.example.ngpro.service;

import com.example.ngpro.model.Customer;
import com.example.ngpro.model.ServiceOrder;
import com.example.ngpro.model.Technician;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.ServiceOrderRepository;
import com.example.ngpro.repository.TechnicianRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ServiceOrderService {

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private IntegrationHub integrationHub;

    private static final DateTimeFormatter ORDER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public ServiceOrder createOrder(ServiceOrder order) {
        if (order.getOrderNumber() == null) {
            String prefix = order.getType() != null ? order.getType().name().substring(0, 3) : "OS";
            order.setOrderNumber(prefix + "-" + LocalDateTime.now().format(ORDER_FORMAT) + "-" + System.currentTimeMillis() % 10000);
        }

        if (order.getCustomerId() != null) {
            Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
            if (customer != null) {
                order.setAddress(customer.getAddress());
                order.setCity(customer.getCity());
                order.setState(customer.getState());
                order.setContactPhone(customer.getPhone());
                order.setContactEmail(customer.getEmail());
            }
        }

        order = serviceOrderRepository.save(order);
        
        log.info("[SERVICE_ORDER] Created order {} for customer {}", order.getOrderNumber(), order.getCustomerId());
        
        if (order.getContactEmail() != null) {
            integrationHub.notifyCustomer(
                order.getCustomerId().toString(), 
                "EMAIL", 
                "Sua OS " + order.getOrderNumber() + " foi criada. Em breve entraremos em contato."
            );
        }

        return order;
    }

    public Optional<ServiceOrder> getOrderById(Long id) {
        return serviceOrderRepository.findById(id);
    }

    public Optional<ServiceOrder> getOrderByNumber(String orderNumber) {
        return serviceOrderRepository.findByOrderNumber(orderNumber);
    }

    public List<ServiceOrder> getOrdersByCustomer(Long customerId) {
        return serviceOrderRepository.findByCustomerId(customerId);
    }

    public List<ServiceOrder> getOrdersByTechnician(Long technicianId) {
        return serviceOrderRepository.findByTechnicianId(technicianId);
    }

    public List<ServiceOrder> getOrdersByStatus(ServiceOrder.ServiceOrderStatus status) {
        return serviceOrderRepository.findByStatus(status);
    }

    public List<ServiceOrder> getAllOrders() {
        return serviceOrderRepository.findAll();
    }

    public List<ServiceOrder> getOpenOrders() {
        return serviceOrderRepository.findByStatusIn(List.of(
            ServiceOrder.ServiceOrderStatus.OPEN,
            ServiceOrder.ServiceOrderStatus.ASSIGNED,
            ServiceOrder.ServiceOrderStatus.IN_PROGRESS
        ));
    }

    public List<ServiceOrder> getScheduledOrders(LocalDateTime start, LocalDateTime end) {
        return serviceOrderRepository.findByScheduledDateBetween(start, end);
    }

    @Transactional
    public ServiceOrder assignTechnician(Long orderId, Long technicianId) {
        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found: " + technicianId));

        if (technician.getCurrentOrdersCount() >= technician.getMaxOrders()) {
            throw new RuntimeException("Technician has reached maximum orders limit");
        }

        order.setTechnicianId(technicianId);
        order.setStatus(ServiceOrder.ServiceOrderStatus.ASSIGNED);
        order = serviceOrderRepository.save(order);

        technician.setCurrentOrdersCount(technician.getCurrentOrdersCount() + 1);
        technicianRepository.save(technician);

        log.info("[SERVICE_ORDER] Assigned order {} to technician {}", order.getOrderNumber(), technician.getName());

        if (technician.getPhone() != null) {
            integrationHub.notifyCustomer(
                technician.getPhone(),
                "SMS",
                "Nova OS atribuída: " + order.getOrderNumber() + " - " + order.getType()
            );
        }

        return order;
    }

    @Transactional
    public ServiceOrder updateStatus(Long orderId, ServiceOrder.ServiceOrderStatus newStatus) {
        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        ServiceOrder.ServiceOrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order = serviceOrderRepository.save(order);

        if (newStatus == ServiceOrder.ServiceOrderStatus.IN_PROGRESS && oldStatus != ServiceOrder.ServiceOrderStatus.IN_PROGRESS) {
            order.setStartedAt(LocalDateTime.now());
            order = serviceOrderRepository.save(order);
        }

        if (newStatus == ServiceOrder.ServiceOrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
            
            if (order.getStartedAt() != null) {
                long minutes = java.time.Duration.between(order.getStartedAt(), order.getCompletedAt()).toMinutes();
                order.setActualDurationMinutes((int) minutes);
            }

            if (order.getTechnicianId() != null) {
                technicianRepository.findById(order.getTechnicianId()).ifPresent(tech -> {
                    tech.setCurrentOrdersCount(Math.max(0, tech.getCurrentOrdersCount() - 1));
                    technicianRepository.save(tech);
                });
            }

            if (order.getType() == ServiceOrder.ServiceOrderType.INSTALLATION) {
                provisioningService.activateService(order.getCustomerId());
            }

            order = serviceOrderRepository.save(order);
        }

        log.info("[SERVICE_ORDER] Order {} status changed from {} to {}", order.getOrderNumber(), oldStatus, newStatus);

        return order;
    }

    @Transactional
    public ServiceOrder completeOrder(Long orderId, String resolution) {
        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setResolution(resolution);
        order.setStatus(ServiceOrder.ServiceOrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        
        if (order.getStartedAt() != null) {
            long minutes = java.time.Duration.between(order.getStartedAt(), order.getCompletedAt()).toMinutes();
            order.setActualDurationMinutes((int) minutes);
        }

        order = serviceOrderRepository.save(order);

        if (order.getCustomerId() != null) {
            integrationHub.notifyCustomer(
                order.getCustomerId().toString(),
                "WHATSAPP",
                "Sua OS " + order.getOrderNumber() + " foi finalizada. Avalie nosso atendimento!"
            );
        }

        log.info("[SERVICE_ORDER] Order {} completed", order.getOrderNumber());

        return order;
    }

    @Transactional
    public ServiceOrder cancelOrder(Long orderId, String reason) {
        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(ServiceOrder.ServiceOrderStatus.CANCELLED);
        order.setObservations((order.getObservations() != null ? order.getObservations() + "\n" : "") + "Cancelamento: " + reason);
        
        if (order.getTechnicianId() != null) {
            technicianRepository.findById(order.getTechnicianId()).ifPresent(tech -> {
                tech.setCurrentOrdersCount(Math.max(0, tech.getCurrentOrdersCount() - 1));
                technicianRepository.save(tech);
            });
        }

        order = serviceOrderRepository.save(order);

        log.info("[SERVICE_ORDER] Order {} cancelled: {}", order.getOrderNumber(), reason);

        return order;
    }

    public ServiceOrder addComment(Long orderId, String message, String userName, String userType, boolean isInternal) {
        log.info("[SERVICE_ORDER] Comment added to order {} by {}", orderId, userName);
        return serviceOrderRepository.findById(orderId).orElse(null);
    }

    public Technician createTechnician(Technician technician) {
        return technicianRepository.save(technician);
    }

    public List<Technician> getAvailableTechnicians() {
        return technicianRepository.findByCurrentOrdersCountLessThan(10);
    }

    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll();
    }

    public Technician findBestTechnician(String region, ServiceOrder.ServiceOrderType type) {
        List<Technician> available = technicianRepository.findByCurrentOrdersCountLessThan(10);
        
        return available.stream()
                .filter(t -> t.getStatus() == Technician.TechnicianStatus.AVAILABLE)
                .filter(t -> t.getRegion() == null || t.getRegion().equalsIgnoreCase(region))
                .findFirst()
                .orElse(null);
    }
}
