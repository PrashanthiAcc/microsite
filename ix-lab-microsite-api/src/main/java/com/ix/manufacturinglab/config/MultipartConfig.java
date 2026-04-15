package com.ix.manufacturinglab.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {

        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Set max file size (per file)
        factory.setMaxFileSize(DataSize.ofMegabytes(200));

        // Set max request size (total request size including all files + JSON)
        factory.setMaxRequestSize(DataSize.ofMegabytes(200));

        return factory.createMultipartConfig();
    }
}
