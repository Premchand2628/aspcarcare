package com.carwash.supportchatservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "carwash_chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // identifies conversation owner (user)
    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String username;

    // "USER" or "ADMIN"
    @Column(length = 20, nullable = false)
    private String sender;

    @Column(length = 1000, nullable = false)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 🔑 link to ChatSession, but stored as a simple id (no JPA relation)
    @Column(name = "session_id")
    private Long sessionId;

    public ChatMessage() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ---------- getters & setters ----------

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getSessionId() {
        return sessionId;
    }
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
