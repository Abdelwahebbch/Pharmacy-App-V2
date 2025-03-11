package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SaleService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private MedicineRepository medicineRepository;
    
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }
    
    public Sale getSaleById(Long id) {
        return saleRepository.findById(id).orElse(null);
    }
    
    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return saleRepository.findBySaleDateBetween(start, end);
    }
    
    public List<Sale> getRecentSales() {
        return saleRepository.findRecentSales();
    }
    
    public BigDecimal getTotalSalesAmount(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return saleRepository.getTotalSalesAmount(start, end);
    }
    
    @Transactional
    public Sale saveSale(Sale sale) {
        // Set sale date if not already set
        if (sale.getSaleDate() == null) {
            sale.setSaleDate(LocalDateTime.now());
        }
        
        // Update stock for each medicine
        for (SaleItem item : sale.getItems()) {
            Medicine medicine = item.getMedicine();
            int newStock = medicine.getStockQuantity() - item.getQuantity();
            
            if (newStock < 0) {
                throw new IllegalStateException("Not enough stock for medicine: " + medicine.getName());
            }
            
            medicine.setStockQuantity(newStock);
            medicineRepository.save(medicine);
        }
        
        return saleRepository.save(sale);
    }
    
    @Transactional
    public void deleteSale(Long id) {
        Sale sale = getSaleById(id);
        if (sale != null) {
            // Restore stock for each medicine
            for (SaleItem item : sale.getItems()) {
                Medicine medicine = item.getMedicine();
                medicine.setStockQuantity(medicine.getStockQuantity() + item.getQuantity());
                medicineRepository.save(medicine);
            }
            
            saleRepository.deleteById(id);
        }
    }
}

