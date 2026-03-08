package com.carwash.supportchatservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "carwash_chat_session")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private String username;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;   // NEW / ACTIVE / CLOSED

    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;

    // NEW: who ended this chat: "USER" or "ADMIN"
    @Column(name = "ended_by", length = 20)
    private String endedBy;

    public ChatSession() {}

    @PrePersist
    public void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (lastActivityAt == null) {
            lastActivityAt = startedAt;
        }
        if (status == null) {
            status = ChatStatus.NEW;   // = “requested”
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now();
        }
    }

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public ChatStatus getStatus() { return status; }
    public void setStatus(ChatStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public String getEndedBy() { return endedBy; }
    public void setEndedBy(String endedBy) { this.endedBy = endedBy; }
}
