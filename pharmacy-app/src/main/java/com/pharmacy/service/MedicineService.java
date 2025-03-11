package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.StockTransaction;
import com.pharmacy.model.User;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {
    
    @Autowired
    private MedicineRepository medicineRepository;
    
    @Autowired
    private StockTransactionService stockTransactionService;
    
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }
    
    public List<Medicine> getAllActiveMedicines() {
        return medicineRepository.findByActiveTrue();
    }
    
    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }
    
    @Transactional
    public Medicine saveMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }
    
    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }
    
    public List<Medicine> searchMedicinesByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategory(category);
    }
    
    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findByExpiryDateBefore(LocalDate.now());
    }
    
    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines();
    }
    
    public List<Medicine> getMedicinesSortedByExpiryDate() {
        return medicineRepository.findAllActiveOrderByExpiryDateAsc();
    }
    
    @Transactional
    public boolean updateStock(Long id, int quantity, User user, String notes) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        medicine.setStockQuantity(medicine.getStockQuantity() + quantity);
        medicineRepository.save(medicine);
        
        // Record the stock transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setMedicine(medicine);
        transaction.setQuantity(quantity);
        transaction.setUser(user);
        transaction.setNotes(notes);
        
        if (quantity > 0) {
            transaction.setType(StockTransaction.TransactionType.STOCK_IN);
        } else {
            transaction.setType(StockTransaction.TransactionType.STOCK_OUT);
        }
        
        stockTransactionService.saveStockTransaction(transaction);
        
        return true;
    }
    
    public List<Medicine> getMedicinesBySupplier(Long supplierId) {
        return medicineRepository.findByActiveTrueAndSupplier_Id(supplierId);
    }
}

