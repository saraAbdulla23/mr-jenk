package com.product_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ProductServiceApplication.class)
@ActiveProfiles("test")
class ProductServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
