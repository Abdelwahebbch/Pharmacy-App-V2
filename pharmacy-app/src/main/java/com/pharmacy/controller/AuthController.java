package com.pharmacy.controller;

import com.pharmacy.model.AuditLog;
import com.pharmacy.model.User;
import com.pharmacy.payload.request.LoginRequest;
import com.pharmacy.payload.request.SignupRequest;
import com.pharmacy.payload.response.JwtResponse;
import com.pharmacy.payload.response.MessageResponse;
import com.pharmacy.security.jwt.JwtUtils;
import com.pharmacy.security.services.UserDetailsImpl;
import com.pharmacy.service.AuditLogService;
import com.pharmacy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserService userService;
    
    @Autowired
    AuditLogService auditLogService;
    
    @Autowired
    JwtUtils jwtUtils;
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            
            // Reset failed attempts
            userService.resetFailedAttempts(userDetails.getUsername());
            
            // Update last login
            userService.updateLastLogin(userDetails.getUsername());
            
            // Log the login
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            auditLogService.createLog(
                    AuditLog.ActionType.LOGIN,
                    "User",
                    user.getId(),
                    "User logged in",
                    user,
                    request.getRemoteAddr()
            );
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    userDetails.getFullName(),
                    roles));
        } catch (Exception e) {
            // Handle failed login attempts
            try {
                User user = userService.getUserByUsername(loginRequest.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                if (user.isAccountNonLocked()) {
                    userService.increaseFailedAttempts(user);
                    
                    if (user.getFailedAttempt() >= UserService.MAX_FAILED_ATTEMPTS - 1) {
                        userService.lock(user);  >= UserService.MAX_FAILED_ATTEMPTS - 1) {
                        userService.lock(user);
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Account locked due to too many failed attempts. Try again after 24 hours."));
                    }
                }
            } catch (UsernameNotFoundException ex) {
                // Do nothing to prevent username enumeration
            }
            
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid username or password"));
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.getUserByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        
        if (userService.getUserByUsername(signUpRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Create new user's account
        User user = userService.registerUser(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                signUpRequest.getFullName()
        );
        
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        // Get the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            try {
                User user = userService.getUserByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                // Log the logout
                auditLogService.createLog(
                        AuditLog.ActionType.LOGOUT,
                        "User",
                        user.getId(),
                        "User logged out",
                        user,
                        request.getRemoteAddr()
                );
            } catch (UsernameNotFoundException ex) {
                // Do nothing
            }
        }
        
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }
}

