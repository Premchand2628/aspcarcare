package com.carwash.supportchatservice.controller;

import com.carwash.supportchatservice.dto.CreateTicketRequest;
import com.carwash.supportchatservice.dto.CreateTicketResponse;
import com.carwash.supportchatservice.entity.SupportTicket;
import com.carwash.supportchatservice.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final SupportTicketRepository ticketRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:aspcaare@no-reply.com}")
    private String fromAddress;

    public TicketController(SupportTicketRepository ticketRepository,
                            JavaMailSender mailSender) {
        this.ticketRepository = ticketRepository;
        this.mailSender = mailSender;
    }

    @PostMapping
    public ResponseEntity<CreateTicketResponse> createTicket(
            @RequestBody CreateTicketRequest req) {

        if (req.getPhone() == null || req.getPhone().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new CreateTicketResponse(false, "Phone is required", null));
        }

        // Build entity
        SupportTicket ticket = new SupportTicket();
        ticket.setUsername(req.getUsername());
        ticket.setPhone(req.getPhone());
        ticket.setEmail(req.getEmail());
        ticket.setIssueType(req.getIssueType());
        ticket.setBookingId(req.getBookingId());
        ticket.setDescription(req.getDescription());

        // Save to DB (prePersist will generate ticketNumber)
        SupportTicket saved = ticketRepository.save(ticket);

        // Send email to user (if email present)
        sendTicketEmail(saved);

        CreateTicketResponse response = new CreateTicketResponse(
                true,
                "Ticket created successfully",
                saved.getTicketNumber()
        );

        return ResponseEntity.ok(response);
    }

    private void sendTicketEmail(SupportTicket ticket) {
        System.out.println("Sending mail to: " + ticket.getEmail());

        if (ticket.getEmail() == null || ticket.getEmail().isBlank()) {
            System.out.println("No email – skipping send.");
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(ticket.getEmail());
            msg.setFrom(fromAddress);
            msg.setSubject(ticket.getTicketNumber());

            String userName = (ticket.getUsername() != null && !ticket.getUsername().isBlank())
                    ? ticket.getUsername()
                    : "Customer";

            String body = "Hi " + userName + "\n\n"
                    + (ticket.getDescription() != null ? ticket.getDescription() : "")
                    + "\n\nThanks\nASP Caare";

            msg.setText(body);
            mailSender.send(msg);

            System.out.println("Mail sent OK");
        } catch (Exception ex) {
            ex.printStackTrace();  // check console for Gmail error like 534/535/etc
        }
    }

    // ================== NEW: ADMIN VIEW OF TICKETS ==================

    /**
     * Admin API to fetch all tickets, newest first.
     * Used by admin.html -> /tickets/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<SupportTicket>> getAllTicketsForAdmin() {
        List<SupportTicket> list = ticketRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(list);
    }
}
