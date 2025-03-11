package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    
    List<Medicine> findByNameContainingIgnoreCase(String name);
    
    List<Medicine> findByCategory(String category);
    
    List<Medicine> findByExpiryDateBefore(LocalDate date);
    
    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity < 10 AND m.active = true")
    List<Medicine> findLowStockMedicines();
    
    @Query("SELECT m FROM Medicine m WHERE m.active = true ORDER BY m.expiryDate ASC")
    List<Medicine> findAllActiveOrderByExpiryDateAsc();
    
    List<Medicine> findByActiveTrue();
    
    List<Medicine> findByActiveTrueAndSupplier_Id(Long supplierId);
}

