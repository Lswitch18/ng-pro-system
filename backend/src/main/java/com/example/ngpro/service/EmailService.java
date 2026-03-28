package com.example.ngpro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ngpro.com.br}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String to, String customerName) {
        log.info("[EMAIL] Sending welcome email to: {} ({})", customerName, to);
        
        if (mailSender == null) {
            log.warn("[EMAIL] JavaMailSender not available. Email simulated only in log.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to NG-PRO Enterprise!");
            message.setText("Hello " + customerName + ",\n\n" +
                            "Your registration in NG-PRO Enterprise system was successful.\n" +
                            "You will receive instructions for your first access soon.\n\n" +
                            "Best regards,\nNG-PRO Team");
            mailSender.send(message);
            log.info("[EMAIL] Welcome email sent successfully.");
        } catch (Exception e) {
            log.error("[EMAIL] Error sending welcome email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendCollectionEmail(String to, String customerName, double amount, String referenceMonth) {
        log.info("[EMAIL] Sending collection email to: {} (R$ {})", customerName, amount);
        
        if (mailSender == null) {
            log.warn("[EMAIL] JavaMailSender not available. Email simulated only in log.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("[NOTICE] Overdue Invoice - NG-PRO Enterprise");
            message.setText("Hello " + customerName + ",\n\n" +
                            "We identified that your invoice is overdue.\n" +
                            "Amount due: R$ " + String.format("%.2f", amount) + "\n" +
                            "Reference: " + referenceMonth + "\n\n" +
                            "To avoid service suspension, please make your payment as soon as possible.\n\n" +
                            "Best regards,\nNG-PRO Team");
            mailSender.send(message);
            log.info("[EMAIL] Collection email sent successfully.");
        } catch (Exception e) {
            log.error("[EMAIL] Error sending collection email: {}", e.getMessage(), e);
        }
    }
}
