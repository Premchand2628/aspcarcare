package com.carwash.supportchatservice.dto;

import com.carwash.supportchatservice.entity.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;

public class StartChatRequest {
    private String phone;
    private String username;
    private String firstMessage; // optional
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
	public String getFirstMessage() {
		return firstMessage;
	}
	public void setFirstMessage(String firstMessage) {
		this.firstMessage = firstMessage;
	}

    
}