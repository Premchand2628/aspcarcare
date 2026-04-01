package com.carwash.otplogin.dto;

public class UpdateProfileRequest {

    private String phone;      // used to identify the user
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String carNumber;
    private String carAddressDefaultFlag;
    private String password;   // new password (optional)

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getCarAddressDefaultFlag() {
        return carAddressDefaultFlag;
    }

    public void setCarAddressDefaultFlag(String carAddressDefaultFlag) {
        this.carAddressDefaultFlag = carAddressDefaultFlag;
    }
}
