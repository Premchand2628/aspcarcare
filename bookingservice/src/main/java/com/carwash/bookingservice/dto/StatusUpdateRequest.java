package com.carwash.bookingservice.dto;

public class StatusUpdateRequest {

    // expected values: PENDING, IN_SERVICING, COMPLETED, CLOSED, CANCELLED, CONFIRMED (uppercase)
    private String status;

    // Optional base64-encoded completion photo (JPEG/PNG), set when status = COMPLETED
    private String completionPhoto;

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

    public String getCompletionPhoto() {
        return completionPhoto;
    }

    public void setCompletionPhoto(String completionPhoto) {
        this.completionPhoto = completionPhoto;
    }
}
