package com.pharmacy.controller;

import com.pharmacy.model.User;
import com.pharmacy.security.services.UserDetailsImpl;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @Autowired
    private MedicineService medicineService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home(Model model) {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && 
                                 authentication.isAuthenticated() && 
                                 !authentication.getPrincipal().equals("anonymousUser");
        
        if (isAuthenticated) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            model.addAttribute("user", user);
            
            // Check if user has admin role
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
            
            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
            
            // Check if user has pharmacist role
            boolean isPharmacist = user.getRoles().stream()
                    .anyMatch(role -> role.getName().name().equals("ROLE_PHARMACIST"));
            
            if (isPharmacist) {
                return "redirect:/pharmacist/dashboard";
            }
            
            // Regular user
            return "redirect:/dashboard";
        }
        
        // Not authenticated, show landing page
        model.addAttribute("featuredMedicines", medicineService.getAllActiveMedicines().subList(0, 
                Math.min(6, medicineService.getAllActiveMedicines().size())));
        
        return "landing";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("medicines", medicineService.getAllActiveMedicines());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines());
        
        return "dashboard";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}

