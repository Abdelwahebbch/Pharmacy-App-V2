package com.pharmacy.repository;

import com.pharmacy.model.MedicineBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineBookingRepository extends JpaRepository<MedicineBooking, Long> {
    
    List<MedicineBooking> findByUser_Id(Long userId);
    
    List<MedicineBooking> findByMedicine_Id(Long medicineId);
    
    List<MedicineBooking> findByPharmacist_Id(Long pharmacistId);
    
    List<MedicineBooking> findByStatus(MedicineBooking.BookingStatus status);
    
    @Query("SELECT mb FROM MedicineBooking mb WHERE mb.expiryDate < ?1 AND mb.status = 'APPROVED'")
    List<MedicineBooking> findExpiredBookings(LocalDate currentDate);
    
    @Query("SELECT mb FROM MedicineBooking mb WHERE mb.user.id = ?1 ORDER BY mb.createdAt DESC")
    List<MedicineBooking> findUserBookingsOrderByCreatedAtDesc(Long userId);
}

