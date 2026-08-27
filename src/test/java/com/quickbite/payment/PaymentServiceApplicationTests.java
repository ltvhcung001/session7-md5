package com.quickbite.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PaymentServiceApplicationTests {

    @Test
    @DisplayName("Context Loads & Application Starts Successfully")
    void contextLoads() {
        assertNotNull(this);
    }
}
