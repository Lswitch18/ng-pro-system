package com.example.ngpro.service;

import com.example.ngpro.model.AuditLog;
import com.example.ngpro.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(String entityType, Long entityId, AuditLog.AuditAction action,
                         String oldValue, String newValue, Long userId, String username,
                         String userRole, String ipAddress, String details) {
        AuditLog audit = new AuditLog();
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setAction(action);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setUserId(userId);
        audit.setUsername(username);
        audit.setUserRole(userRole);
        audit.setIpAddress(ipAddress);
        audit.setDetails(details);
        
        auditLogRepository.save(audit);
        log.info("[AUDIT] {} {} - {} by {}", action, entityType, entityId, username);
    }

    public void logLogin(Long userId, String username, String ipAddress, boolean success) {
        logAction("AUTH", userId, success ? AuditLog.AuditAction.LOGIN : AuditLog.AuditAction.LOGIN_FAILED,
                null, null, userId, username, null, ipAddress, null, null);
    }

    public void logCreate(String entityType, Long entityId, String newValue, Long userId, String username, String userRole) {
        logAction(entityType, entityId, AuditLog.AuditAction.CREATE, null, newValue, 
                userId, username, userRole, null, null);
    }

    public void logUpdate(String entityType, Long entityId, String oldValue, String newValue, 
                         Long userId, String username, String userRole) {
        logAction(entityType, entityId, AuditLog.AuditAction.UPDATE, oldValue, newValue, 
                userId, username, userRole, null, null);
    }

    public void logDelete(String entityType, Long entityId, String oldValue, 
                         Long userId, String username, String userRole) {
        logAction(entityType, entityId, AuditLog.AuditAction.DELETE, oldValue, null, 
                userId, username, userRole, null, null);
    }

    public Page<AuditLog> getAuditLogs(int page, int size) {
        return auditLogRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AuditLog> getAuditLogsByAction(AuditLog.AuditAction action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    public Optional<AuditLog> getAuditLogById(Long id) {
        return auditLogRepository.findById(id);
    }
}
