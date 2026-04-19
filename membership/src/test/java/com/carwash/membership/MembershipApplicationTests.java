package com.carwash.membership;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires full application context with DB - enable when real tests are added")
class MembershipApplicationTests {
}
