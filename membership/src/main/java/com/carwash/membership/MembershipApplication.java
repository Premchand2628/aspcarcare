package com.carwash.membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
<<<<<<< HEAD
	    "com.carwash.membership",      // service root
	    "com.carwashcommon"            // shared security/common root
=======
	    "com.carwash.membership",      // your service root
	    //"com.carwashcommon"         // your shared library root
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
	})
public class MembershipApplication {

	public static void main(String[] args) {
		SpringApplication.run(MembershipApplication.class, args);
	}

}
