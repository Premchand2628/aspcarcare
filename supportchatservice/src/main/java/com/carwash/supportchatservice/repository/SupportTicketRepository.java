package com.carwash.supportchatservice.repository;

import com.carwash.supportchatservice.entity.SupportTicket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

	List<SupportTicket> findAllByOrderByCreatedAtDesc();
    // you can add findByTicketNumber etc later if needed
}
