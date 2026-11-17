package com.example.shop.sharedkernel;

/** Raised by application services when an aggregate id does not resolve. Mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String what, Object id) {
        super(what + " not found: " + id);
    }
}
