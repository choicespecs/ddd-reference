package com.example.shop.config;

import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps exceptions to RFC 7807/9457 "problem+json" responses (see {@link ApiProblem}). The domain
 * throws plain exceptions; only this web-layer class knows about HTTP status codes.
 *
 * <p>Extending ResponseEntityExceptionHandler and overriding handleExceptionInternal gives
 * Spring's own errors (malformed JSON, Bean Validation failures, wrong method…) the same shape.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Object> notFound(NotFoundException ex, WebRequest request) {
        return ApiProblem.of(HttpStatus.NOT_FOUND, "not-found", "Resource not found",
                ex.getMessage(), path(request)).toResponse();
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<Object> ruleViolated(DomainException ex, WebRequest request) {
        return ApiProblem.of(HttpStatus.CONFLICT, "business-rule", "Business rule violated",
                ex.getMessage(), path(request)).toResponse();
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<Object> concurrentModification(ObjectOptimisticLockingFailureException ex, WebRequest request) {
        return ApiProblem.of(HttpStatus.CONFLICT, "concurrent-modification", "Concurrent modification",
                "The resource was changed by someone else. Reload and try again.", path(request)).toResponse();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Object> badInput(IllegalArgumentException ex, WebRequest request) {
        return ApiProblem.of(HttpStatus.BAD_REQUEST, "bad-input", "Invalid input",
                ex.getMessage(), path(request)).toResponse();
    }

    /** Bean Validation failure on a @Valid @RequestBody: 400 with per-field messages. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return ApiProblem.of(HttpStatus.BAD_REQUEST, "validation", "Invalid request",
                        "One or more fields are invalid", path(request))
                .with("errors", errors)
                .toResponse();
    }

    /** Every other Spring MVC exception (400/405/415…) gets the same problem+json shape. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ApiProblem.of(status, "http-" + status.value(), status.getReasonPhrase(),
                ex.getMessage(), path(request)).toResponse();
    }

    private static String path(WebRequest request) {
        return request instanceof ServletWebRequest
                ? ((ServletWebRequest) request).getRequest().getRequestURI()
                : null;
    }
}
