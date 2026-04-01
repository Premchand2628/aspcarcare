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

    // ---- slot availability (service type only) ----
    @Query("""
           SELECT COUNT(b)
           FROM Booking b
           WHERE b.bookingDate = :date
             AND b.timeSlot = :slot
             AND UPPER(b.serviceType) = :serviceType
            AND UPPER(COALESCE(b.status, 'PENDING')) NOT IN ('CANCELLED', 'CLOSED')
           """)
    long countForAvailability(
            @Param("date") LocalDate date,
            @Param("slot") String slot,
           @Param("serviceType") String serviceType
    );

    // ---- slot availability for a specific service centre id ----
    @Query("""
          SELECT COUNT(b)
          FROM Booking b
          WHERE b.bookingDate = :date
            AND b.timeSlot = :slot
            AND UPPER(b.serviceType) = :serviceType
            AND b.serviceCentreId = :centreId
            AND UPPER(COALESCE(b.status, 'PENDING')) NOT IN ('CANCELLED', 'CLOSED')
          """)
    long countForAvailabilityByCentreId(
           @Param("date") LocalDate date,
           @Param("slot") String slot,
           @Param("serviceType") String serviceType,
           @Param("centreId") Long centreId
    );

    // ---- slot availability for a specific service centre name (fallback) ----
    @Query("""
          SELECT COUNT(b)
          FROM Booking b
          WHERE b.bookingDate = :date
            AND b.timeSlot = :slot
            AND UPPER(b.serviceType) = :serviceType
            AND UPPER(COALESCE(b.centreName, '')) = :centreName
            AND UPPER(COALESCE(b.status, 'PENDING')) NOT IN ('CANCELLED', 'CLOSED')
          """)
    long countForAvailabilityByCentreName(
           @Param("date") LocalDate date,
           @Param("slot") String slot,
           @Param("serviceType") String serviceType,
           @Param("centreName") String centreName
    );

    // ---- slot availability for a specific service centre id + address ----
    @Query("""
          SELECT COUNT(b)
          FROM Booking b
          WHERE b.bookingDate = :date
            AND b.timeSlot = :slot
            AND UPPER(b.serviceType) = :serviceType
            AND b.serviceCentreId = :centreId
            AND REPLACE(UPPER(COALESCE(b.address, '')), ' ', '') = :normalizedAddress
            AND UPPER(COALESCE(b.status, 'PENDING')) NOT IN ('CANCELLED', 'CLOSED')
          """)
    long countForAvailabilityByCentreIdAndAddress(
           @Param("date") LocalDate date,
           @Param("slot") String slot,
           @Param("serviceType") String serviceType,
           @Param("centreId") Long centreId,
           @Param("normalizedAddress") String normalizedAddress
    );

    // ---- slot availability for a specific service centre name + address ----
    @Query("""
          SELECT COUNT(b)
          FROM Booking b
          WHERE b.bookingDate = :date
            AND b.timeSlot = :slot
            AND UPPER(b.serviceType) = :serviceType
            AND UPPER(COALESCE(b.centreName, '')) = :centreName
            AND REPLACE(UPPER(COALESCE(b.address, '')), ' ', '') = :normalizedAddress
            AND UPPER(COALESCE(b.status, 'PENDING')) NOT IN ('CANCELLED', 'CLOSED')
          """)
    long countForAvailabilityByCentreNameAndAddress(
           @Param("date") LocalDate date,
           @Param("slot") String slot,
           @Param("serviceType") String serviceType,
           @Param("centreName") String centreName,
           @Param("normalizedAddress") String normalizedAddress
    );
    
    long countByPhone(String phone);

       @Query(value = "SELECT nextval('carwash_booking_home_seq')", nativeQuery = true)
       Long nextHomeBookingCodeSeq();

       @Query(value = "SELECT nextval('carwash_booking_self_seq')", nativeQuery = true)
       Long nextSelfBookingCodeSeq();

}
