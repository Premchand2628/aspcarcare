package com.carwash.otplogin.dto;

public class SaveDefaultBookingRequest {

    private String address;
    private String carNumber;
    private Boolean saveAsDefault;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public Boolean getSaveAsDefault() {
        return saveAsDefault;
    }

    public void setSaveAsDefault(Boolean saveAsDefault) {
        this.saveAsDefault = saveAsDefault;
    }
}
