package com.carwash.otplogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {
	    "com.carwash.otplogin",      // your service root
	    "com.carwashcommon"         // your shared library root
	})
//@SpringBootApplication
public class AuthuserApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthuserApplication.class, args);
    }
}