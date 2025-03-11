package com.pharmacy.controller;

import com.pharmacy.model.AuditLog;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.User;
import com.pharmacy.security.services.UserDetailsImpl;
import com.pharmacy.service.AuditLogService;
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
@RequestMapping("/medicines")
public class MedicineController {
    
    @Autowired
    private MedicineService medicineService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @GetMapping
    public String getAllMedicines(Model model) {
        model.addAttribute("medicines", medicineService.getAllActiveMedicines());
        return "medicines/list";
    }
    
    @GetMapping("/{id}")
    public String getMedicineById(@PathVariable Long id, Model model, HttpServletRequest request) {
        Medicine medicine = medicineService.getMedicineById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        model.addAttribute("medicine", medicine);
        
        // Log the view
        logAction(AuditLog.ActionType.VIEW, "Medicine", id, "Viewed medicine details", request);
        
        return "medicines/view";
    }
    
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public String showNewMedicineForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        return "medicines/form";
    }
    
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public String showEditMedicineForm(@PathVariable Long id, Model model) {
        Medicine medicine = medicineService.getMedicineById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        model.addAttribute("medicine", medicine);
        return "medicines/form";
    }
    
    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public String saveMedicine(@Valid @ModelAttribute Medicine medicine, HttpServletRequest request) {
        boolean isNew = (medicine.getId() == null);
        
        Medicine savedMedicine = medicineService.saveMedicine(medicine);
        
        // Log the action
        if (isNew) {
            logAction(AuditLog.ActionType.CREATE, "Medicine", savedMedicine.getId(), 
                    "Created new medicine: " + savedMedicine.getName(), request);
        } else {
            logAction(AuditLog.ActionType.UPDATE, "Medicine", savedMedicine.getId(), 
                    "Updated medicine: " + savedMedicine.getName(), request);
        }
        
        return "redirect:/medicines";
    }
    
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMedicine(@PathVariable Long id, HttpServletRequest request) {
        Medicine medicine = medicineService.getMedicineById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        medicineService.deleteMedicine(id);
        
        // Log the action
        logAction(AuditLog.ActionType.DELETE, "Medicine", id, 
                "Deleted medicine: " + medicine.getName(), request);
        
        return "redirect:/medicines";
    }
    
    @GetMapping("/search")
    public String searchMedicines(@RequestParam String keyword, Model model) {
        model.addAttribute("medicines", medicineService.searchMedicinesByName(keyword));
        model.addAttribute("keyword", keyword);
        return "medicines/list";
    }
    
    @GetMapping("/category/{category}")
    public String getMedicinesByCategory(@PathVariable String category, Model model) {
        model.addAttribute("medicines", medicineService.getMedicinesByCategory(category));
        model.addAttribute("category", category);
        return "medicines/list";
    }
    
    @GetMapping("/expired")
    public String getExpiredMedicines(Model model) {
        model.addAttribute("medicines", medicineService.getExpiredMedicines());
        model.addAttribute("title", "Expired Medicines");
        return "medicines/list";
    }
    
    @GetMapping("/low-stock")
    public String getLowStockMedicines(Model model) {
        model.addAttribute("medicines", medicineService.getLowStockMedicines());
        model.addAttribute("title", "Low Stock Medicines");
        return "medicines/list";
    }
    
    @PostMapping("/stock/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<?> updateStock(
            @RequestParam Long id,
            @RequestParam int quantity,
            @RequestParam String notes,
            HttpServletRequest request) {
        
        User user = getCurrentUser();
        
        boolean success = medicineService.updateStock(id, quantity, user, notes);
        
        if (success) {
            // Log the action
            logAction(AuditLog.ActionType.UPDATE, "Medicine", id, 
                    "Updated stock by " + quantity + " units. Notes: " + notes, request);
            
            return ResponseEntity.ok().body("Stock updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update stock");
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

