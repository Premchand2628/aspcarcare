package com.carwash.bookingservice.dto;

public class StatusUpdateRequest {

    // expected values: PENDING, IN_SERVICING, CLOSED (uppercase)
    private String status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
