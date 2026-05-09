package com.carwash.bookingservice.dto;

import java.math.BigDecimal;

import com.carwash.bookingservice.entity.ServiceCentre;

public class ServiceCentreDto {

    private Long id;
    private String centreCode;
    private String name;
    private String area;
    private String address;
    private String pincode;
    private String city;
    private String state;
    private BigDecimal rating;
    private BigDecimal basePrice;
    private String mapsUrl;
    private Double lat;
    private Double lng;
    private Boolean active;

    // ---- factory helpers ----
    public static ServiceCentreDto fromEntity(ServiceCentre c) {
        ServiceCentreDto dto = new ServiceCentreDto();
        dto.id = c.getId();
        dto.centreCode = c.getCentreCode();
        dto.name = c.getName();
        dto.area = c.getArea();
        dto.address = c.getAddress();
        dto.pincode = c.getPincode();
        dto.city = c.getCity();
        dto.state = c.getState();
        dto.rating = c.getRating();
        dto.basePrice = c.getBasePrice();
        dto.mapsUrl = c.getMapsUrl();
        dto.lat = c.getLatitude();
        dto.lng = c.getLongitude();
        dto.active = c.getActive();
        return dto;
    }

    public ServiceCentre toEntity() {
        ServiceCentre c = new ServiceCentre();
        c.setCentreCode(this.centreCode);
        c.setName(this.name);
        c.setArea(this.area);
        c.setAddress(this.address);
        c.setPincode(this.pincode);
        c.setCity(this.city);
        c.setState(this.state);
        c.setRating(this.rating);
        c.setBasePrice(this.basePrice);
        c.setMapsUrl(this.mapsUrl);
        c.setLatitude(this.lat);
        c.setLongitude(this.lng);
        if (this.active != null) c.setActive(this.active);
        return c;
    }

    // ===== Getters & Setters (or use Lombok) =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCentreCode() { return centreCode; }
    public void setCentreCode(String centreCode) { this.centreCode = centreCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public String getMapsUrl() { return mapsUrl; }
    public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
