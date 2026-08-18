package com.diksha.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central exception -> HTTP response mapping. This file existed but was
 * completely empty, so every business error (e.g. "Student not found",
 * "No active study plan found", "Access denied") was previously falling
 * through to Spring Boot's default error page as a generic 500 with no
 * usable message.
 * <p>
 * Response shape is {@code {"message": "...", "status": ..., "timestamp": ...}}
 * to match what the Angular frontend already reads everywhere via
 * {@code error.error?.message} (see login.ts, register.ts, teacher-dashboard.ts,
 * student-dashboard.ts, admin-students.ts).
 * <p>
 * Most of the service layer throws plain {@link RuntimeException} with a
 * descriptive message rather than typed exceptions, so the status code is
 * inferred from the message content below. If you introduce typed
 * exceptions later (NotFoundException, ForbiddenException, ...), add
 * dedicated @ExceptionHandler methods for them above the generic
 * RuntimeException handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "You are not authorized to perform this action");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Validation failed");
        return body(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Something went wrong";
        return body(inferStatus(message), message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    /**
     * The service layer uses plain RuntimeException with descriptive text
     * instead of typed exceptions (e.g. "Student not found", "Access denied",
     * "Email already exists"), so we infer the right HTTP status from
     * keywords in the message rather than the exception's Java type.
     */
    private HttpStatus inferStatus(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (lower.contains("denied") || lower.contains("not allowed") || lower.contains("not authorized")) {
            return HttpStatus.FORBIDDEN;
        }
        if (lower.contains("already exists") || lower.contains("already enrolled")) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<Object> body(HttpStatus status, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", message);
        payload.put("status", status.value());
        payload.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(payload);
    }
}
