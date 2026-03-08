package com.carwash.bookingservice.dto;

public class ApiResponse {
    private boolean success;
    private String message;
    private String token; // optional - for login success
<<<<<<< HEAD
    private String bookingCode;
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
<<<<<<< HEAD

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
}
