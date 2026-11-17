package com.example.shop.config;

import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

/**
 * Maps exceptions to RFC 9457 "problem+json" responses. The domain throws plain exceptions;
 * only this web-layer class knows about HTTP status codes.
 *
 * <p>Extending ResponseEntityExceptionHandler gives consistent ProblemDetail bodies for Spring's
 * own errors too (e.g. Bean Validation failures → 400 with field details).
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "not-found", "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail ruleViolated(DomainException ex) {
        return problem(HttpStatus.CONFLICT, "business-rule", "Business rule violated", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail concurrentModification(ObjectOptimisticLockingFailureException ex) {
        return problem(HttpStatus.CONFLICT, "concurrent-modification", "Concurrent modification",
                "The resource was changed by someone else. Reload and try again.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badInput(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "bad-input", "Invalid input", ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://example.com/problems/" + type));
        pd.setTitle(title);
        return pd;
    }
}
