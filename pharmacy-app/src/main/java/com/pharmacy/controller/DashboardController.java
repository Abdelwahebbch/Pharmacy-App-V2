package com.pharmacy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.pharmacy.service.MedicineService;

@Controller
public class DashboardController {
    
    @Autowired
    private MedicineService medicineService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines().size());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines().size());
        // You can add more data for the dashboard here
        return "dashboard";
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}

