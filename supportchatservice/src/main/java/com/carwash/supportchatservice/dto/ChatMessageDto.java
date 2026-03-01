package com.carwash.supportchatservice.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {

    private Long id;
    private String sender;
    private String message;
    private LocalDateTime createdAt;

    public ChatMessageDto(Long id, String sender, String message, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }
    public String getSender() {
        return sender;
    }
    public String getMessage() {
        return message;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
