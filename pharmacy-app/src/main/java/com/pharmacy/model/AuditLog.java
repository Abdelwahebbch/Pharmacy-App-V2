package com.pharmacy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private ActionType action;
    
    private String entityType;
    
    private Long entityId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String ipAddress;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum ActionType {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT, VIEW
    }
}

