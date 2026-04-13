package com.ix.manufacturinglab;

//import com.ix.manufacturinglab.config.CorsProperties;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Industry-X Manufacturing Lab Microsite API.
 */
@SpringBootApplication
@ComponentScan("com.ix")
@EnableScheduling
@EnableAsync
//@EnableConfigurationProperties(CorsProperties.class)
public class ManufacturingLabApplication {

    private static final String APPLICATION_LABEL_KEY = "APPLICATION-LABEL";

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        MDC.put(APPLICATION_LABEL_KEY, "IX");
        SpringApplication.run(ManufacturingLabApplication.class, args);
    }
}
