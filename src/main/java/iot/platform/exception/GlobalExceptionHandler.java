package iot.platform.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest request) {
        log.warn("Domain exception at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(ex.getStatus(), ex.getStatus().getReasonPhrase(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        log.warn("Validation failed at {}: {} violations", request.getRequestURI(), violations.size());
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(localized("error.validation", "Validation failed"))
                .path(request.getRequestURI())
                .violations(violations)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.info("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Forbidden", localized("error.forbidden", "Access denied"), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        log.info("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", localized("error.unauthorized", "Authentication required"), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Conflict", localized("error.conflict", "Data integrity violation"), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Not Found", localized("error.notFound", "Resource not found"), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                localized("error.internal", "An unexpected error occurred"), request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message, HttpServletRequest request) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private ApiError.FieldViolation toViolation(FieldError fe) {
        return ApiError.FieldViolation.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .build();
    }

    private String localized(String key, String fallback) {
        try {
            return messageSource.getMessage(key, null, fallback, LocaleContextHolder.getLocale());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }
}
