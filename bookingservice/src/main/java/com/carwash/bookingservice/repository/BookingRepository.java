package com.carwash.bookingservice.repository;

import com.carwash.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPhoneOrderByCreatedAtDesc(String phone);

    List<Booking> findByBookingDate(LocalDate bookingDate);
    List<Booking> findByBookingDateAndServiceType(LocalDate bookingDate, String serviceType);


    List<Booking> findAllByOrderByCreatedAtDesc();

    boolean existsByTransactionId(String transactionId);

    // ---- slot availability for Home + SelfDrive ----
    @Query("""
           SELECT COUNT(b)
           FROM Booking b
           WHERE b.bookingDate = :date
             AND b.timeSlot = :slot
             AND UPPER(b.serviceType) = :serviceType
             AND b.status <> 'CANCELLED'
             AND (
                    :centreId IS NULL
                    OR b.serviceCentreId = :centreId
                    OR b.serviceCentreId IS NULL
                 )
           """)
    long countForAvailability(
            @Param("date") LocalDate date,
            @Param("slot") String slot,
            @Param("serviceType") String serviceType,
            @Param("centreId") Long centreId
    );
    
    long countByPhone(String phone);

}
