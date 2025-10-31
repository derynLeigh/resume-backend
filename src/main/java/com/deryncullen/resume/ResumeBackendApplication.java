package com.deryncullen.resume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ResumeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeBackendApplication.class, args);
    }
}
