package com.travel_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TravelServiceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TravelServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("Hello from Travel Service");
    }
}