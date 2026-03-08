package com.carwash.supportchatservice.dto;

public class EndChatRequest {

    private Long sessionId;   // optional – we can also find by phone
    private String phone;     // user phone
    private String endedBy;   // "USER" or "ADMIN"

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

    public String getEndedBy() {
        return endedBy;
    }
    public void setEndedBy(String endedBy) {
        this.endedBy = endedBy;
    }
}
