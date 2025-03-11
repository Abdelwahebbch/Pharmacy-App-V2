package com.pharmacy.repository;

import com.pharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_PHARMACIST'")
    List<User> findAllPharmacists();
    
    @Modifying
    @Query("UPDATE User u SET u.failedAttempt = ?1 WHERE u.username = ?2")
    void updateFailedAttempts(int failedAttempt, String username);
    
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = ?1, u.lockTime = ?2 WHERE u.username = ?3")
    void updateAccountLockStatus(boolean accountNonLocked, LocalDateTime lockTime, String username);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = ?1 WHERE u.username = ?2")
    void updateLastLogin(LocalDateTime lastLogin, String username);
}

