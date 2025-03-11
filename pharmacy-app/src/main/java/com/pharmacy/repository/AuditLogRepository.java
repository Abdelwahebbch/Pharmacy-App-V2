package com.pharmacy.repository;

import com.pharmacy.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUser_Id(Long userId);
    
    List<AuditLog> findByAction(AuditLog.ActionType action);
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN ?1 AND ?2 ORDER BY al.createdAt DESC")
    List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al ORDER BY al.createdAt DESC LIMIT 100")
    List<AuditLog> findRecentLogs();
}

