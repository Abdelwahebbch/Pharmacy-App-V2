package com.pharmacy.repository;

import com.pharmacy.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    
    List<StockTransaction> findByMedicine_Id(Long medicineId);
    
    List<StockTransaction> findByUser_Id(Long userId);
    
    List<StockTransaction> findByType(StockTransaction.TransactionType type);
    
    @Query("SELECT st FROM StockTransaction st WHERE st.createdAt BETWEEN ?1 AND ?2")
    List<StockTransaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}

