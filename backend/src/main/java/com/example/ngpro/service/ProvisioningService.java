package com.example.ngpro.service;

import com.example.ngpro.model.Customer;
import com.example.ngpro.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProvisioningService {

    @Autowired
    private IntegrationHub integrationHub;

    @Autowired
    private CustomerRepository customerRepository;

    public void activateService(Long customerId) {
        log.info("[PROVISIONING] Activating service for customer [{}]", customerId);
        integrationHub.authorizeRadiusAccess(customerId.toString(), true);
        updateStatus(customerId, "ACTIVE");
    }

    public void suspendService(Long customerId) {
        log.info("[PROVISIONING] Suspending service for customer [{}] (Inadimplência)", customerId);
        integrationHub.authorizeRadiusAccess(customerId.toString(), false);
        // Desconecta o usuário no Radius para forçar reautenticação com bloqueio
        integrationHub.notifyCustomer(customerId.toString(), "RADIUS", "DISCONNECT_COMMAND_SENT");
        updateStatus(customerId, "SUSPENDED");
    }

    public void blockService(Long customerId) {
        log.info("[PROVISIONING] Blocking service for customer [{}] (Cancelamento/Fraude)", customerId);
        integrationHub.authorizeRadiusAccess(customerId.toString(), false);
        updateStatus(customerId, "BLOCKED");
    }

    private void updateStatus(Long id, String status) {
        customerRepository.findById(id).ifPresent(c -> {
            c.setStatus(status);
            customerRepository.save(c);
        });
    }
}
