package com.pharmacy.controller;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
@RequestMapping("/sales")
public class SaleController {
    
    @Autowired
    private SaleService saleService;
    
    @Autowired
    private MedicineService medicineService;
    
    @GetMapping
    public String getAllSales(Model model) {
        model.addAttribute("sales", saleService.getAllSales());
        return "sales/list";
    }
    
    @GetMapping("/new")
    public String showNewSaleForm(Model model) {
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setItems(new ArrayList<>());
        
        model.addAttribute("sale", sale);
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "sales/form";
    }
    
    @PostMapping("/save")
    public String saveSale(@ModelAttribute Sale sale) {
        saleService.saveSale(sale);
        return "redirect:/sales";
    }
    
    @GetMapping("/view/{id}")
    public String viewSale(@PathVariable Long id, Model model) {
        Sale sale = saleService.getSaleById(id);
        model.addAttribute("sale", sale);
        return "sales/view";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return "redirect:/sales";
    }
    
    @GetMapping("/report")
    public String showSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sales", saleService.getSalesByDateRange(startDate, endDate));
        model.addAttribute("totalAmount", saleService.getTotalSalesAmount(startDate, endDate));
        
        return "sales/report";
    }
    
    @GetMapping("/add-item")
    public String addItemToSale(@RequestParam Long medicineId, @RequestParam Integer quantity, Model model) {
        Medicine medicine = medicineService.getMedicineById(medicineId);
        SaleItem item = new SaleItem();
        item.setMedicine(medicine);
        item.setQuantity(quantity);
        item.setUnitPrice(medicine.getPrice());
        item.calculateSubtotal();
        
        model.addAttribute("item", item);
        return "sales/item-row :: item-row";
    }
}

