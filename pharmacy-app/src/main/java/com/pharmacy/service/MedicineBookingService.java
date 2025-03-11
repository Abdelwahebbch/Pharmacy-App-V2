package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.MedicineBooking;
import com.pharmacy.model.User;
import com.pharmacy.repository.MedicineBookingRepository;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineBookingService {
    
    @Autowired
    private MedicineBookingRepository bookingRepository;
    
    @Autowired
    private MedicineRepository medicineRepository;
    
    @Autowired
    private MedicineService medicineService;
    
    public List<MedicineBooking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    public Optional<MedicineBooking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }
    
    public List<MedicineBooking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUser_Id(userId);
    }
    
    public List<MedicineBooking> getBookingsByMedicine(Long medicineId) {
        return bookingRepository.findByMedicine_Id(medicineId);
    }
    
    public List<MedicineBooking> getBookingsByPharmacist(Long pharmacistId) {
        return bookingRepository.findByPharmacist_Id(pharmacistId);
    }
    
    public List<MedicineBooking> getBookingsByStatus(MedicineBooking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
    
    public List<MedicineBooking> getExpiredBookings() {
        return bookingRepository.findExpiredBookings(LocalDate.now());
    }
    
    public List<MedicineBooking> getUserBookingsOrderByCreatedAtDesc(Long userId) {
        return bookingRepository.findUserBookingsOrderByCreatedAtDesc(userId);
    }
    
    @Transactional
    public MedicineBooking createBooking(MedicineBooking booking) {
        // Set default values
        if (booking.getBookingDate() == null) {
            booking.setBookingDate(LocalDate.now());
        }
        
        if (booking.getExpiryDate() == null) {
            booking.setExpiryDate(LocalDate.now().plusDays(7)); // Default expiry is 7 days
        }
        
        booking.setStatus(MedicineBooking.BookingStatus.PENDING);
        
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public MedicineBooking updateBooking(MedicineBooking booking) {
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }
    
    @Transactional
    public MedicineBooking approveBooking(Long id, User pharmacist) {
        MedicineBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        booking.setStatus(MedicineBooking.BookingStatus.APPROVED);
        booking.setPharmacist(pharmacist);
        
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public MedicineBooking rejectBooking(Long id, User pharmacist, String notes) {
        MedicineBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        booking.setStatus(MedicineBooking.BookingStatus.REJECTED);
        booking.setPharmacist(pharmacist);
        booking.setNotes(notes);
        
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public MedicineBooking completeBooking(Long id, User pharmacist) {
        MedicineBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        booking.setStatus(MedicineBooking.BookingStatus.COMPLETED);
        booking.setPharmacist(pharmacist);
        
        // Reduce stock
        Medicine medicine = booking.getMedicine();
        medicineService.updateStock(medicine.getId(), -booking.getQuantity(), pharmacist, 
                "Completed booking #" + booking.getId());
        
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public MedicineBooking cancelBooking(Long id, String notes) {
        MedicineBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        booking.setStatus(MedicineBooking.BookingStatus.CANCELLED);
        booking.setNotes(notes);
        
        return bookingRepository.save(booking);
    }
}

