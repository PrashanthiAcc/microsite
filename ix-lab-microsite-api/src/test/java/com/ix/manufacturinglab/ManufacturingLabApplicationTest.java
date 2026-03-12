package com.ix.manufacturinglab;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test for the main application context loading.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ManufacturingLabApplicationTest {

    @Test
    public void contextLoads() {
        // Verifies the application context loads successfully
    }
}
