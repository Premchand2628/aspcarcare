package com.carwash.supportchatservice.dto;

public class CreateTicketResponse {

    private boolean success;
    private String message;
    private String ticketNumber;

    public CreateTicketResponse() {}

    public CreateTicketResponse(boolean success, String message, String ticketNumber) {
        this.success = success;
        this.message = message;
        this.ticketNumber = ticketNumber;
    }

    // getters & setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
}
