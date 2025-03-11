package com.pharmacy.service;

import com.pharmacy.model.ERole;
import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.RoleRepository;
import com.pharmacy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    
    public static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 hours
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<User> getAllPharmacists() {
        return userRepository.findAllPharmacists();
    }
    
    @Transactional
    public User registerUser(String username, String email, String password, String fullName) {
        User user = new User(username, email, passwordEncoder.encode(password), fullName);
        
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    @Transactional
    public User registerPharmacist(String username, String email, String password, String fullName) {
        User user = new User(username, email, passwordEncoder.encode(password), fullName);
        
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Role pharmacistRole = roleRepository.findByName(ERole.ROLE_PHARMACIST)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        
        roles.add(userRole);
        roles.add(pharmacistRole);
        
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    @Transactional
    public User registerAdmin(String username, String email, String password, String fullName) {
        User user = new User(username, email, passwordEncoder.encode(password), fullName);
        
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        
        roles.add(userRole);
        roles.add(adminRole);
        
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Transactional
    public void increaseFailedAttempts(User user) {
        int newFailedAttempts = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempts(newFailedAttempts, user.getUsername());
    }
    
    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.updateFailedAttempts(0, username);
    }
    
    @Transactional
    public void lock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());
        
        userRepository.save(user);
    }
    
    @Transactional
    public boolean unlockWhenTimeExpired(User user) {
        LocalDateTime lockTime = user.getLockTime();
        if (lockTime != null) {
            LocalDateTime currentTime = LocalDateTime.now();
            if (lockTime.plusHours(24).isBefore(currentTime)) {
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedAttempt(0);
                
                userRepository.save(user);
                return true;
            }
        }
        
        return false;
    }
    
    @Transactional
    public void updateLastLogin(String username) {
        userRepository.updateLastLogin(LocalDateTime.now(), username);
    }
}

