package com.example.shop.sharedkernel;

import java.time.Instant;

/** Marker for something that happened in the domain that other parts of the system may care about. */
public interface DomainEvent {
    Instant occurredAt();
}
