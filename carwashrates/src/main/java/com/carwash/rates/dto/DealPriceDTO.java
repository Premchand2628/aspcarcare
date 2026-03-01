package com.carwash.rates.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealPriceDTO {
    private Long id;
    private String dealServiceType;
    private String dealWashType;
    private String dealCarType;
    private String dealWaterProviding;
    private BigDecimal dealActualPrice;
    private BigDecimal dealDiscount;
    private BigDecimal dealFinalPrice;
}
