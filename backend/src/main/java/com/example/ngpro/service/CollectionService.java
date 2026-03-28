package com.example.ngpro.service;

import com.example.ngpro.model.Invoice;
import com.example.ngpro.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class CollectionService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private IntegrationHub integrationHub;

    /**
     * Simula a emissão de cobrança (Arrecadação)
     */
    public String emitCollection(Invoice invoice) {
        String pixCode = "00020126580014br.gov.bcb.pix0136" + UUID.randomUUID().toString().substring(0, 20);
        log.info("[COLLECTION] Emitting PIX/Boleto for Invoice [{}] | Amount: R${}", 
                invoice.getId(), invoice.getTotalAmount());
        
        integrationHub.notifyCustomer(invoice.getCustomerId().toString(), "WHATSAPP", 
                "Sua fatura de " + invoice.getReferenceMonth() + " está pronta. Valor: R$" + invoice.getTotalAmount());
        
        return pixCode;
    }

    /**
     * Simula a conciliação bancária automática
     */
    public void processConciliation(Long invoiceId) {
        invoiceRepository.findById(invoiceId).ifPresent(inv -> {
            log.info("[CONCILIATION] Payment confirmed for Invoice [{}]", invoiceId);
            inv.setStatus("PAID");
            inv.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(inv);
            
            integrationHub.notifyCustomer(inv.getCustomerId().toString(), "SMS", "Recebemos seu pagamento. Obrigado!");
        });
    }
}
