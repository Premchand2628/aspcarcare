package com.carwash.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
    "com.carwash.bookingservice",
    "com.carwash.mailnotification",
    "com.carwash.otplogin"
})
@EnableJpaRepositories(basePackages = {
    "com.carwash.bookingservice.repository",
    "com.carwash.otplogin.repository"
})
@EntityScan(basePackages = {
    "com.carwash.bookingservice.entity",
    "com.carwash.otplogin.entity"
})
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
//@SpringBootApplication
//public class BookingServiceApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(BookingServiceApplication.class, args);
//    }
//}