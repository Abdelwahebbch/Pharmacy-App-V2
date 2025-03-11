package com.pharmacy.controller;

import com.pharmacy.model.AuditLog;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.MedicineBooking;
import com.pharmacy.model.User;
import com.pharmacy.security.services.UserDetailsImpl;
import com.pharmacy.service.AuditLogService;
import com.pharmacy.service.MedicineBookingService;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/bookings")
public class MedicineBookingController {
    
    @Autowired
    private MedicineBookingService bookingService;
    
    @Autowired
    private MedicineService medicineService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @GetMapping
    public String getAllBookings(Model model) {
        User currentUser = getCurrentUser();
        
        // Check if user has admin or pharmacist role
        boolean isAdminOrPharmacist = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN") || 
                                 role.getName().name().equals("ROLE_PHARMACIST"));
        
        List<MedicineBooking> bookings;
        
        if (isAdminOrPharmacist) {
            bookings = bookingService.getAllBookings();
        } else {
            bookings = bookingService.getBookingsByUser(currentUser.getId());
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("isAdminOrPharmacist", isAdminOrPharmacist);
        
        return "bookings/list";
    }
    
    @GetMapping("/{id}")
    public String getBookingById(@PathVariable Long id, Model model, HttpServletRequest request) {
        MedicineBooking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        User currentUser = getCurrentUser();
        
        // Check if user has permission to view this booking
        boolean hasPermission = booking.getUser().getId().equals(currentUser.getId()) ||
                currentUser.getRoles().stream()
                        .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN") || 
                                         role.getName().name().equals("ROLE_PHARMACIST"));
        
        if (!hasPermission) {
            return "redirect:/bookings?error=unauthorized";
        }
        
        model.addAttribute("booking", booking);
        
        // Log the view
        logAction(AuditLog.ActionType.VIEW, "MedicineBooking", id, "Viewed booking details", request);
        
        return "bookings/view";
    }
    
    @GetMapping("/new")
    public String showNewBookingForm(Model model) {
        model.addAttribute("booking", new MedicineBooking());
        model.addAttribute("medicines", medicineService.getAllActiveMedicines());
        return "bookings/form";
    }
    
    @GetMapping("/new/{medicineId}")
    public String showNewBookingFormForMedicine(@PathVariable Long medicineId, Model model) {
        Medicine medicine = medicineService.getMedicineById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + medicineId));
        
        MedicineBooking booking = new MedicineBooking();
        booking.setMedicine(medicine);
        
        model.addAttribute("booking", booking);
        model.addAttribute("medicine", medicine);
        
        return "bookings/form";
    }
    
    @PostMapping("/save")
    public String saveBooking(@Valid @ModelAttribute MedicineBooking booking, HttpServletRequest request) {
        User currentUser = getCurrentUser();
        booking.setUser(currentUser);
        
        MedicineBooking savedBooking = bookingService.createBooking(booking);
        
        // Log the action
        logAction(AuditLog.ActionType.CREATE, "MedicineBooking", savedBooking.getId(), 
                "Created new booking for medicine: " + savedBooking.getMedicine().getName(), request);
        
        return "redirect:/bookings";
    }
    
    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, @RequestParam(required = false) String notes, 
                               HttpServletRequest request) {
        MedicineBooking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        User currentUser = getCurrentUser();
        
        // Check if user has permission to cancel this booking
        boolean hasPermission = booking.getUser().getId().equals(currentUser.getId()) ||
                currentUser.getRoles().stream()
                        .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
        
        if (!hasPermission) {
            return "redirect:/bookings?error=unauthorized";
        }
        
        bookingService.cancelBooking(id, notes);
        
        // Log the action
        logAction(AuditLog.ActionType.UPDATE, "MedicineBooking", id, 
                "Cancelled booking for medicine: " + booking.getMedicine().getName(), request);
        
        return "redirect:/bookings";
    }
    
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<?> approveBooking(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUser();
        
        try {
            MedicineBooking booking = bookingService.approveBooking(id, currentUser);
            
            // Log the action
            logAction(AuditLog.ActionType.UPDATE, "MedicineBooking", id, 
                    "Approved booking for medicine: " + booking.getMedicine().getName(), request);
            
            return ResponseEntity.ok().body("Booking approved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to approve booking: " + e.getMessage());
        }
    }
    
    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id, @RequestParam String notes, 
                                         HttpServletRequest request) {
        User currentUser = getCurrentUser();
        
        try {
            MedicineBooking booking = bookingService.rejectBooking(id, currentUser, notes);
            
            // Log the action
            logAction(AuditLog.ActionType.UPDATE, "MedicineBooking", id, 
                    "Rejected booking for medicine: " + booking.getMedicine().getName() + 
                    ". Reason: " + notes, request);
            
            return ResponseEntity.ok().body("Booking rejected successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reject booking: " + e.getMessage());
        }
    }
    
    @PostMapping("/complete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<?> completeBooking(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUser();
        
        try {
            MedicineBooking booking = bookingService.completeBooking(id, currentUser);
            
            // Log the action
            logAction(AuditLog.ActionType.UPDATE, "MedicineBooking", id, 
                    "Completed booking for medicine: " + booking.getMedicine().getName(), request);
            
            return ResponseEntity.ok().body("Booking completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to complete booking: " + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    
    private void logAction(AuditLog.ActionType action, String entityType, Long entityId, String details, HttpServletRequest request) {
        try {
            User user = getCurrentUser();
            auditLogService.createLog(action, entityType, entityId, details, user, request.getRemoteAddr());
        } catch (Exception e) {
            // Log error but don't stop the main operation
            System.err.println("Error logging action: " + e.getMessage());
        }
    }
}

