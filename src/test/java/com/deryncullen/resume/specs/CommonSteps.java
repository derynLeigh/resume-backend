package com.deryncullen.resume.specs;

import com.deryncullen.resume.ResumeBackendApplication;
import com.thoughtworks.gauge.AfterSuite;
import com.thoughtworks.gauge.Step;
import io.restassured.RestAssured;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Common steps shared across all Gauge specs
 */
public class CommonSteps {

    private static ConfigurableApplicationContext context;

    @Step("Start the application")
    public void startApplication() {
        if (context == null) {
            context = SpringApplication.run(ResumeBackendApplication.class,
                    "--server.port=8081",
                    "--spring.profiles.active=test");
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = 8081;
            RestAssured.basePath = "/api";
        }
    }

    @AfterSuite
    public void tearDown() {
        if (context != null && context.isActive()) {
            context.close();
            context = null;
        }
    }
}