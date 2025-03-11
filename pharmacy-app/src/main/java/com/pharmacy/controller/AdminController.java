package com.pharmacy.controller;

import com.pharmacy.model.AuditLog;
import com.pharmacy.model.User;
import com.pharmacy.security.services.UserDetailsImpl;
import com.pharmacy.service.AuditLogService;
import com.pharmacy.service.MedicineBookingService;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MedicineService medicineService;
    
    @Autowired
    private MedicineBookingService bookingService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalPharmacists", userService.getAllPharmacists().size());
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines().size());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines().size());
        model.addAttribute("pendingBookings", bookingService.getBookingsByStatus(MedicineBooking.BookingStatus.PENDING).size());
        
        model.addAttribute("recentLogs", auditLogService.getRecentLogs());
        
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String getAllUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
    
    @GetMapping("/pharmacists")
    public String getAllPharmacists(Model model) {
        model.addAttribute("pharmacists", userService.getAllPharmacists());
        return "admin/pharmacists";
    }
    
    @GetMapping("/pharmacists/new")
    public String showNewPharmacistForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/pharmacist-form";
    }
    
    @PostMapping("/pharmacists/save")
    public String savePharmacist(@Valid @ModelAttribute User user, HttpServletRequest request) {
        User savedUser = userService.registerPharmacist(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName()
        );
        
        // Log the action
        logAction(AuditLog.ActionType.CREATE, "User", savedUser.getId(), 
                "Created new pharmacist: " + savedUser.getUsername(), request);
        
        return "redirect:/admin/pharmacists";
    }
    
    @GetMapping("/pharmacists/delete/{id}")
    public String deletePharmacist(@PathVariable Long id, HttpServletRequest request) {
        User pharmacist = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        userService.deleteUser(id);
        
        // Log the action
        logAction(AuditLog.ActionType.DELETE, "User", id, 
                "Deleted pharmacist: " + pharmacist.getUsername(), request);
        
        return "redirect:/admin/pharmacists";
    }
    
    @GetMapping("/logs")
    public String viewLogs(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        model.addAttribute("logs", auditLogService.getLogsByDateRange(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "admin/logs";
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

