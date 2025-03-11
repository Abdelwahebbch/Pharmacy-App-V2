package com.pharmacy.service;

import com.pharmacy.model.StockTransaction;
import com.pharmacy.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class StockTransactionService {
    
    @Autowired
    private StockTransactionRepository transactionRepository;
    
    public List<StockTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<StockTransaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public List<StockTransaction> getTransactionsByMedicine(Long medicineId) {
        return transactionRepository.findByMedicine_Id(medicineId);
    }
    
    public List<StockTransaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUser_Id(userId);
    }
    
    public List<StockTransaction> getTransactionsByType(StockTransaction.TransactionType type) {
        return transactionRepository.findByType(type);
    }
    
    public List<StockTransaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return transactionRepository.findByDateRange(start, end);
    }
    
    public StockTransaction saveStockTransaction(StockTransaction transaction) {
        return transactionRepository.save(transaction);
    }
}

