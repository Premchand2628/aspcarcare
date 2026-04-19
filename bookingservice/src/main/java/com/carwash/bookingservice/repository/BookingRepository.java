package com.carwash.bookingservice.repository;

import com.carwash.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // ---- Centre-specific queries for CentreApp (match by centre_name) ----

    @Query("""
          SELECT b FROM Booking b
          WHERE UPPER(COALESCE(b.centreName, '')) = UPPER(:centreName)
            AND b.status IN :statuses
          ORDER BY b.bookingDate DESC, b.timeSlot ASC
          """)
    List<Booking> findByCentreNameAndStatusIn(
            @Param("centreName") String centreName,
            @Param("statuses") List<String> statuses);

    @Query("""
          SELECT b FROM Booking b
          WHERE UPPER(COALESCE(b.centreName, '')) = UPPER(:centreName)
            AND b.status IN :statuses
          ORDER BY b.createdAt DESC
          """)
    List<Booking> findByCentreNameAndDoneStatuses(
            @Param("centreName") String centreName,
            @Param("statuses") List<String> statuses);

    @Query("""
          SELECT b FROM Booking b
          WHERE UPPER(COALESCE(b.centreName, '')) = UPPER(:centreName)
            AND b.phone = :phone
            AND b.status NOT IN :excludeStatuses
          ORDER BY b.createdAt DESC
          """)
    List<Booking> findByCentreNameAndPhoneExcludingStatuses(
            @Param("centreName") String centreName,
            @Param("phone") String phone,
            @Param("excludeStatuses") List<String> excludeStatuses);

    @Query("""
          SELECT COUNT(b) FROM Booking b
          WHERE UPPER(COALESCE(b.centreName, '')) = UPPER(:centreName)
            AND b.status = :status
          """)
    long countByCentreNameAndStatus(
            @Param("centreName") String centreName,
            @Param("status") String status);

    @Query("""
          SELECT COUNT(b) FROM Booking b
          WHERE UPPER(COALESCE(b.centreName, '')) = UPPER(:centreName)
            AND b.status IN :statuses
          """)
    long countByCentreNameAndStatusIn(
            @Param("centreName") String centreName,
            @Param("statuses") List<String> statuses);

    @Query(value = "SELECT nextval('carwash_booking_home_seq')", nativeQuery = true)
       Long nextHomeBookingCodeSeq();

       @Query(value = "SELECT nextval('carwash_booking_self_seq')", nativeQuery = true)
       Long nextSelfBookingCodeSeq();

    // ---- Admin Dashboard Stats ----

    long count();

    long countByStatus(String status);

    long countByBookingDate(LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(b.payableAmount), 0) FROM Booking b WHERE b.status IN :statuses")
    java.math.BigDecimal sumRevenueByStatuses(@Param("statuses") List<String> statuses);

    @Query("SELECT COALESCE(SUM(b.payableAmount), 0) FROM Booking b WHERE b.status IN :statuses AND b.bookingDate = :date")
    java.math.BigDecimal sumRevenueByStatusesAndDate(@Param("statuses") List<String> statuses, @Param("date") LocalDate date);

    @Query(value = """
          SELECT CAST(b.booking_date AS VARCHAR), COUNT(*)
          FROM carwash_booking b
          WHERE b.booking_date >= :since
          GROUP BY b.booking_date
          ORDER BY b.booking_date
          """, nativeQuery = true)
    List<Object[]> countPerDay(@Param("since") LocalDate since);

    @Query(value = """
          SELECT COALESCE(b.wash_type, 'UNKNOWN'), COUNT(*)
          FROM carwash_booking b
          GROUP BY b.wash_type
          """, nativeQuery = true)
    List<Object[]> countByWashType();

    @Query(value = """
          SELECT COALESCE(b.car_type, 'UNKNOWN'), COUNT(*)
          FROM carwash_booking b
          GROUP BY b.car_type
          """, nativeQuery = true)
    List<Object[]> countByCarType();

    @Query(value = """
          SELECT COALESCE(b.service_type, 'UNKNOWN'), COUNT(*)
          FROM carwash_booking b
          GROUP BY b.service_type
          """, nativeQuery = true)
    List<Object[]> countByServiceType();

    @Query(value = """
          SELECT COALESCE(b.centre_name, 'UNKNOWN'), COUNT(*)
          FROM carwash_booking b
          GROUP BY b.centre_name
          ORDER BY COUNT(*) DESC
          """, nativeQuery = true)
    List<Object[]> countByCentre();

    @Query("SELECT COUNT(DISTINCT b.phone) FROM Booking b")
    long countDistinctUsers();

    @Query("SELECT COUNT(DISTINCT b.phone) FROM Booking b WHERE b.bookingDate = :date")
    long countDistinctUsersByDate(@Param("date") LocalDate date);

}
