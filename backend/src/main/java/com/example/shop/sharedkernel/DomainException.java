package com.example.shop.sharedkernel;

/**
 * Thrown when a business rule (invariant) is violated. The web layer maps it to
 * HTTP 409/422 via {@code GlobalExceptionHandler}; the domain itself knows nothing about HTTP.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
