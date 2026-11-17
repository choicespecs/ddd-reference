package com.example.shop.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 7807 / RFC 9457 "problem details" body: {@code {type, title, status, detail, instance}}
 * served as {@code application/problem+json}.
 *
 * <p>Spring Framework 6 (Boot 3) ships this as {@code org.springframework.http.ProblemDetail};
 * on Spring Boot 2.7 / Java 11 we define the same shape ourselves. If you later upgrade to
 * Boot 3, delete this class and use Spring's — the JSON the frontend sees stays identical.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiProblem {

    public static final MediaType MEDIA_TYPE = MediaType.valueOf("application/problem+json");

    private final URI type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    private ApiProblem(URI type, String title, int status, String detail, String instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public static ApiProblem of(HttpStatus status, String type, String title, String detail, String instance) {
        return new ApiProblem(URI.create("https://example.com/problems/" + type), title, status.value(), detail, instance);
    }

    /** Extra members, e.g. {@code errors} for field-level validation messages. */
    public ApiProblem with(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public ResponseEntity<Object> toResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MEDIA_TYPE);
        return new ResponseEntity<>(this, headers, HttpStatus.valueOf(status));
    }

    public URI getType() { return type; }
    public String getTitle() { return title; }
    public int getStatus() { return status; }
    public String getDetail() { return detail; }
    public String getInstance() { return instance; }

    @JsonAnyGetter
    public Map<String, Object> getProperties() { return properties; }
}
