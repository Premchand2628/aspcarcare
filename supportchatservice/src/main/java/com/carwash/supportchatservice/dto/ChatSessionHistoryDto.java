package com.carwash.supportchatservice.dto;

import com.carwash.supportchatservice.entity.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ChatSessionHistoryDto {

    private Long sessionId;
    private ChatStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    private List<ChatMessageDto> messages;

    public Long getSessionId() {
        return sessionId;
    }
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public ChatStatus getStatus() {
        return status;
    }
    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }
    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public List<ChatMessageDto> getMessages() {
        return messages;
    }
    public void setMessages(List<ChatMessageDto> messages) {
        this.messages = messages;
    }
}
