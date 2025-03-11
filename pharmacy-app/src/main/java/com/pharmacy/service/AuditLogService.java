package com.pharmacy.service;

import com.pharmacy.model.AuditLog;
import com.pharmacy.model.User;
import com.pharmacy.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
    
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUser_Id(userId);
    }
    
    public List<AuditLog> getLogsByAction(AuditLog.ActionType action) {
        return auditLogRepository.findByAction(action);
    }
    
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    public List<AuditLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return auditLogRepository.findByDateRange(start, end);
    }
    
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findRecentLogs();
    }
    
    public AuditLog createLog(AuditLog.ActionType action, String entityType, Long entityId, String details, User user, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setUser(user);
        log.setIpAddress(ipAddress);
        
        return auditLogRepository.save(log);
    }
}

