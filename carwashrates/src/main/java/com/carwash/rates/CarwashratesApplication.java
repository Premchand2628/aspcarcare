package com.carwash.rates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CarwashratesApplication {
  public static void main(String[] args) {
    SpringApplication.run(CarwashratesApplication.class, args);
  }
}
