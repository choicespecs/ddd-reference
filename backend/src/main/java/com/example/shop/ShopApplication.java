package com.example.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. One deployable (a "modular monolith") containing two bounded contexts:
 * {@code catalog} and {@code ordering}. Each context is a top-level package with its own
 * domain / application / infrastructure / web layers.
 */
@SpringBootApplication
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
