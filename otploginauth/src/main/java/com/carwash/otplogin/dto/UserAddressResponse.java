package com.carwash.otplogin.dto;

import com.carwash.otplogin.entity.UserAddress;

public class UserAddressResponse {
    private Long id;
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
    private boolean defaultAddress;

    public static UserAddressResponse from(UserAddress entity) {
        UserAddressResponse r = new UserAddressResponse();
        r.id = entity.getId();
        r.label = entity.getLabel();
        r.fullName = entity.getFullName();
        r.phone = entity.getPhone();
        r.zipcode = entity.getZipcode();
        r.area = entity.getArea();
        r.streetAddress = entity.getStreetAddress();
        r.city = entity.getCity();
        r.state = entity.getState();
        r.landmark = entity.getLandmark();
        r.latitude = entity.getLatitude();
        r.longitude = entity.getLongitude();
        r.defaultAddress = entity.isDefaultAddress();
        return r;
    }

    public Long getId() { return id; }
    public String getLabel() { return label; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getZipcode() { return zipcode; }
    public String getArea() { return area; }
    public String getStreetAddress() { return streetAddress; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getLandmark() { return landmark; }
    public java.math.BigDecimal getLatitude() { return latitude; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public boolean isDefaultAddress() { return defaultAddress; }
}
