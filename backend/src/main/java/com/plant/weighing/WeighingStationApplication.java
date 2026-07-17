package com.plant.weighing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point. Packages the Angular build (in src/main/resources/static)
 * together with the REST API and scale/SAP integration into a single
 * runnable JAR, designed to run as a Windows Service on the weigh-station PC.
 */
@SpringBootApplication
@EnableScheduling
public class WeighingStationApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeighingStationApplication.class, args);
    }
}
