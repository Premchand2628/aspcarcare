package com.carwash.supportchatservice.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/agent-chat")
@CrossOrigin(origins = "*")
public class AgentChatNotificationController {

    // Simple in-memory store (per app instance)
    private final List<AgentChatNotification> pending = Collections.synchronizedList(new ArrayList<>());

    @PostMapping("/notify")
    public void notifyAgent(@RequestBody AgentChatNotification req) {
        if (req == null) return;
        if (req.getPhone() == null || req.getPhone().isBlank()) return;

        if (req.getCreatedAt() == null) {
            req.setCreatedAt(LocalDateTime.now());
        }
        pending.add(req);
    }

    /**
     * Admin polls this endpoint.
     * Returns and clears all pending notifications.
     */
    @GetMapping("/pending")
    public List<AgentChatNotification> getPending() {
        synchronized (pending) {
            List<AgentChatNotification> copy = new ArrayList<>(pending);
            pending.clear();
            return copy;
        }
    }

    // Simple DTO
    public static class AgentChatNotification {
        private String username;
        private String phone;
        private LocalDateTime createdAt;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
