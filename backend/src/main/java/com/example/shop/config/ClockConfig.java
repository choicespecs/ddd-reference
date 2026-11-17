package com.example.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Injecting a Clock (instead of calling Instant.now()) makes time-dependent domain logic testable. */
@Configuration
class ClockConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
