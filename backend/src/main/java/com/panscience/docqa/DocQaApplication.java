package com.panscience.docqa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DocQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocQaApplication.class, args);
    }
}
