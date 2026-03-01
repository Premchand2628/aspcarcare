package com.carwash.supportchatservice.dto;

import com.carwash.supportchatservice.entity.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;

public class StartChatResponse {
    private Long sessionId;
    private String phone;
    private ChatStatus status;

    // ctor + getters/setters
    public StartChatResponse(Long sessionId, String phone, ChatStatus status) {
        this.sessionId = sessionId;
        this.phone = phone;
        this.status = status;
    }

	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public ChatStatus getStatus() {
		return status;
	}

	public void setStatus(ChatStatus status) {
		this.status = status;
	}
    
}