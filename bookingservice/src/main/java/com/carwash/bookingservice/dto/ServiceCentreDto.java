package com.carwash.bookingservice.dto;

import java.math.BigDecimal;

import com.carwash.bookingservice.entity.ServiceCentre;

public class ServiceCentreDto {

    private Long id;
    private String name;
    private String area;
    private String address;
    private BigDecimal rating;
    private String mapsUrl;
    private Double lat;
    private Double lng;

    // ---- factory helpers ----
    public static ServiceCentreDto fromEntity(ServiceCentre c) {
        ServiceCentreDto dto = new ServiceCentreDto();
        dto.id = c.getId();
        dto.name = c.getName();
        dto.area = c.getArea();
        dto.address = c.getAddress();
        dto.rating = c.getRating();
        dto.mapsUrl = c.getMapsUrl();
        dto.lat = c.getLatitude();
        dto.lng = c.getLongitude();
        return dto;
    }

    public ServiceCentre toEntity() {
        ServiceCentre c = new ServiceCentre();
        c.setName(this.name);
        c.setArea(this.area);
        c.setAddress(this.address);
        c.setRating(this.rating);
        c.setMapsUrl(this.mapsUrl);
        c.setLatitude(this.lat);
        c.setLongitude(this.lng);
        return c;
    }

    // ===== Getters & Setters (or use Lombok) =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getMapsUrl() { return mapsUrl; }
    public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}
