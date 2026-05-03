package com.carwash.otplogin.dto;

public class UserAddressRequest {
    private String label;
    private String fullName;
    private String phone;
    private String zipcode;
    private String area;
    private String streetAddress;
    private String city;
    private String state;
    private String landmark;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private Boolean defaultAddress;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
    public java.math.BigDecimal getLatitude() { return latitude; }
    public void setLatitude(java.math.BigDecimal latitude) { this.latitude = latitude; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public void setLongitude(java.math.BigDecimal longitude) { this.longitude = longitude; }
    public Boolean getDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(Boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}
