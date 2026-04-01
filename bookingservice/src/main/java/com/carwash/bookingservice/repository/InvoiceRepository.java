package com.carwash.bookingservice.repository;

import com.carwash.bookingservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByBookingId(Long bookingId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
